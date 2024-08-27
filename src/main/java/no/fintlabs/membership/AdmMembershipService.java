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
import java.util.stream.Stream;

@Slf4j
@Service
public class AdmMembershipService {
    private final FintCache<String , User> userCache;
    private final UserService userService;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final RoleService roleService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;
    private final ArbeidsforholdService arbeidsforholdService;
    private final MembershipService membershipService;

    public AdmMembershipService(
            FintCache<String, User> userCache, UserService userService, FintCache<String,
            PersonalressursResource> personalressursResourceCache,
            OrganisasjonselementService organisasjonselementService,
            RoleService roleService, FintCache<String, RoleCatalogRole> roleCatalogRoleCache,
            ArbeidsforholdService arbeidsforholdService, MembershipService membershipService
    ) {
        this.userCache = userCache;
        this.userService = userService;
        this.personalressursResourceCache = personalressursResourceCache;
        this.organisasjonselementService = organisasjonselementService;
        this.roleService = roleService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
        this.arbeidsforholdService = arbeidsforholdService;
        this.membershipService = membershipService;
    }

    public List<Membership> createOrgUnitMembershipList ( OrganisasjonselementResource organisasjonselementResource, Date currentTime ) {

        log.debug("Creating Membership list for org unit {} ({})"
                , organisasjonselementResource.getOrganisasjonsId()
                ,organisasjonselementResource.getOrganisasjonsKode());


        Stream<Membership> allMemberships = organisasjonselementService.getAllArbeidsforhold(organisasjonselementResource)
                .stream()
                .map(arbeidsforholdResource -> createMembership(organisasjonselementResource, arbeidsforholdResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(Membership -> {
                    log.debug("Membership with memberid {} has status {}",
                            Membership.getMemberId(), Membership.getMemberStatus());
                } );
        return getUniqueMemberships(allMemberships);
    }

    //TODO: Implement this method
//    public List<Membership> createAggregatedOrgUnitMembershipList(Role role, RoleService roleService) {
//        List<Role> allRoles = new ArrayList<Role>();
//        allRoles.add(role);
//
//        List<Role> aggregatedRoles = role.getChildrenRoleIds()
//                .stream()
//                .map(RoleRef::getRoleRef)
//                .map(roleService::getOptionalRole)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//
//        aggregatedRoles.stream().forEach(aggrrole -> allRoles.add(aggrrole));
//
//        Stream<Membership> allMemberships = allRoles
//                .stream()
//                .flatMap(aggrRole -> aggrRole.getMemberships().stream());
//
//        return getUniqueMemberships(allMemberships);
//    }


    private Optional<Membership> createMembership(
            OrganisasjonselementResource organisasjonselementResource,
            ArbeidsforholdResource arbeidsforholdResource,
            Date currentTime) {

        String roleId = roleService.createRoleId(
                organisasjonselementResource,
                RoleType.ANSATT.getRoleType(),
                RoleSubType.ORGANISASJONSELEMENT.getRoleSubType(),
                false
        );

        Optional<RoleCatalogRole> roleCatalogRole = membershipService.roleService.getRoleCatalogRole(roleId);

        if (roleCatalogRole.isEmpty()) {
            return Optional.empty();
        }

        Optional<PersonalressursResource> personalressursResource = arbeidsforholdService.getPersonalressurs(arbeidsforholdResource);

        if (personalressursResource.isEmpty()) {
            return Optional.empty();
        }
;
        Optional<User> user = userService.getUser(personalressursResource.get().getAnsattnummer().getIdentifikatorverdi());

        if (user.isEmpty()) {
            return Optional.empty();
        }
        String userStatus = user.get().getStatus();

        if (userStatus != null && !userStatus.equals("ACTIVE")) {
            return Optional.of(membershipService.createMembership(roleCatalogRole.get(),
                    user.get(),
                    userStatus,
                    user.get().getStatusChanged()));
        }
        MembershipStatus membershipStatus = MembershipUtils.getArbeidsforholdStatus(arbeidsforholdResource, currentTime);

        return Optional.of(membershipService.createMembership(
                roleCatalogRole.get(),
                user.get(),
                membershipStatus.status(),
                membershipStatus.statusChanged())
        );
    }
    private static List<Membership> getUniqueMemberships(Stream<Membership> allMemberships) {
        return allMemberships
                .collect(Collectors.toMap(Membership::getMemberId,
                        Function.identity(),
                        (a, b) -> "ACTIVE".equalsIgnoreCase(a.getMemberStatus()) ? a : b,
                        LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }
}

