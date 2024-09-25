package no.fintlabs.membership;


import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.role.*;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import no.fintlabs.utils.MembershipUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class AdmMembershipService {
    private final FintCache<String , User> userCache;
    private final UserService userService;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final RoleService roleService;
    //private final RoleCatalogRoleService roleCatalogRoleService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;
    private final ArbeidsforholdService arbeidsforholdService;
    private final MembershipService membershipService;

    public AdmMembershipService(
            FintCache<String, User> userCache, UserService userService, FintCache<String,
            PersonalressursResource> personalressursResourceCache,
            OrganisasjonselementService organisasjonselementService,
            RoleService roleService,
            //RoleCatalogRoleService roleCatalogRoleService,
            FintCache<String, RoleCatalogRole> roleCatalogRoleCache,
            ArbeidsforholdService arbeidsforholdService, MembershipService membershipService
    ) {
        this.userCache = userCache;
        this.userService = userService;
        this.personalressursResourceCache = personalressursResourceCache;
        this.organisasjonselementService = organisasjonselementService;
        this.roleService = roleService;
        //this.roleCatalogRoleService = roleCatalogRoleService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
        this.arbeidsforholdService = arbeidsforholdService;
        this.membershipService = membershipService;
    }

    public List<Membership> createOrgUnitMembershipList ( OrganisasjonselementResource organisasjonselementResource, Date currentTime ) {

        log.info("Creating Membership list for org unit {} ({})"
                , organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()
                ,organisasjonselementResource.getOrganisasjonsKode().getIdentifikatorverdi());


        List<Membership> allMemberships = organisasjonselementService.getAllArbeidsforhold(organisasjonselementResource)
                .stream()
                .map(arbeidsforholdResource -> createOrgUnitMembership(organisasjonselementResource, arbeidsforholdResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(Membership -> {
                    log.info("Membership with memberid {} has status {}",
                            Membership.getMemberId(), Membership.getMemberStatus());
                } )
                .toList();

        return getUniqueMemberships(allMemberships);
    }

    public List<Membership> createAggregatedOrgUnitMembershipList(Role role) {

        log.info("Start creating membership list aggregated role for {} orgUnit Id {}"
                ,role.getOrganisationUnitName()
                , role.getOrganisationUnitId());

        Optional<RoleCatalogRole> aggregatedRoleCatalogRole = roleCatalogRoleCache.getOptional(role.getRoleId());

        if (aggregatedRoleCatalogRole.isEmpty()) {
            log.warn("RoleCatalogRole not found for role {}", role.getRoleId());
            return List.of();
        }
        Long aggregatedRoleId = aggregatedRoleCatalogRole.get().getId();

        if (role.getChildrenRoleIds() == null || role.getChildrenRoleIds().isEmpty()) {
            log.info("Role {} has no children roles", role.getRoleId());
            return List.of();
        }
        List<Membership> allMemberships = role.getChildrenRoleIds()
                .stream()
                .filter(Objects::nonNull)
                .map(RoleRef::getRoleRef)
                .map(roleService::getOptionalRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(aggrRole -> membershipService.createMembershipList(aggrRole).stream())
                .map(membership -> new Membership(
                        aggregatedRoleId,
                        membership.getMemberId(),
                        membership.getMemberStatus(),
                        membership.getMemberStatusChanged())
                )
                .peek(membership -> {
                    log.info("Aggregated membership roleid {} memberid {} has status {}",
                            membership.getRoleId(),
                            membership.getMemberId(),
                            membership.getMemberStatus());
                } )
                .collect(toList());

        List<Membership> unigueMemberships = getUniqueMemberships(allMemberships);

        log.info("Done creating membership list aggregated role for {} orgUnit Id {}"
                ,role.getOrganisationUnitName()
                , role.getOrganisationUnitId());

        return unigueMemberships;
    }
    private Optional<Membership> createOrgUnitMembership(
            OrganisasjonselementResource organisasjonselementResource,
            ArbeidsforholdResource arbeidsforholdResource,
            Date currentTime) {

        log.info("Creating org unit membership for org unit {} ({}) and arbeidsforhold {}",
                organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi(),
                organisasjonselementResource.getOrganisasjonsKode().getIdentifikatorverdi(),
                arbeidsforholdResource.getSystemId().getIdentifikatorverdi()
        );

        String roleId = roleService.createRoleId(
                organisasjonselementResource,
                RoleType.ANSATT.getRoleType(),
                RoleSubType.ORGANISASJONSELEMENT.getRoleSubType(),
                false
        );

        Optional<RoleCatalogRole> optionalRoleCatalogRole = roleService.getRoleCatalogRole(roleId);

        if (optionalRoleCatalogRole.isEmpty()) {
            log.warn("RoleCatalogRole not found for role {}. Org unit role membership not created",
                    roleId
            );
            return Optional.empty();
        }
        RoleCatalogRole roleCatalogRole = optionalRoleCatalogRole.get();

        if (roleCatalogRole.getRoleStatus()==null) {
            log.warn("Role catalog role found, but role status not found for role {}. Org unit role membership not created",
                    roleId
            );
            return Optional.empty();
        }

        Optional<PersonalressursResource> personalressursResource = arbeidsforholdService.getPersonalressurs(arbeidsforholdResource);

        if (personalressursResource.isEmpty()) {
            log.warn("Personalressurs resource not found for arbeidsforhold {}. Org unit role membership not created",
                    arbeidsforholdResource.getSystemId()
            );
            return Optional.empty();
        }
;
        Optional<User> user = userService.getUser(personalressursResource.get().getAnsattnummer().getIdentifikatorverdi());

        if (user.isEmpty()) {
            log.warn("Kontroll user not found for personalressurs {}. Org unit role membership not created",
                    personalressursResource.get().getAnsattnummer().getIdentifikatorverdi()
            );
            return Optional.empty();
        }
        if (roleCatalogRole.getRoleStatus().equals("INACTIVE")) {
            log.info("Role {} is INACTIVE. Membership status for member {} is set to INACTIVE",
                    roleId,
                    user.get().getId()
            );
            return Optional.of(
                    membershipService.createMembership(optionalRoleCatalogRole.get(),
                    user.get(),
                    "INACTIVE",
                    roleCatalogRole.getRoleStatusChanged())
            );
        }
        String userStatus = user.get().getStatus();

        if (userStatus != null && !userStatus.equals("ACTIVE")) {
            return Optional.of(
                    membershipService.createMembership(optionalRoleCatalogRole.get(),
                    user.get(),
                    userStatus,
                    user.get().getStatusChanged())
            );
        }
        MembershipStatus membershipStatus = MembershipUtils.getArbeidsforholdStatus(arbeidsforholdResource, currentTime);

        return Optional.of(
                membershipService.createMembership(
                optionalRoleCatalogRole.get(),
                user.get(),
                membershipStatus.status(),
                membershipStatus.statusChanged())
        );
    }
    private static List<Membership> getUniqueMemberships(List<Membership> allMemberships) {
        return allMemberships
                .stream()
                .collect(Collectors.toMap(Membership::getMemberId,
                        Function.identity(),
                        (a, b) -> "ACTIVE".equalsIgnoreCase(a.getMemberStatus()) ? a : b,
                        LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }
}

