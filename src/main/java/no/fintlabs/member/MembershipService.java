package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleRef;
import no.fintlabs.role.RoleService;
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
public class MembershipService {
    private final FintCache<String , User> userCache;
    private final UserService userService;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;

    public MembershipService(
            FintCache<String, User> userCache, UserService userService, FintCache<String,
            PersonalressursResource> personalressursResourceCache,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService
    ) {
        this.userCache = userCache;
        this.userService = userService;
        this.personalressursResourceCache = personalressursResourceCache;
        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
    }

    public List<Membership> createOrgUnitMembershipList ( OrganisasjonselementResource organisasjonselementResource, Date currentTime ) {

        log.debug("Creating Membership list for org unit {} ({})"
                , organisasjonselementResource.getOrganisasjonsId()
                ,organisasjonselementResource.getOrganisasjonsKode());

        Stream<Membership> allMemberships = organisasjonselementService.getAllArbeidsforhold(organisasjonselementResource)
                .stream()
                .map(arbeidsforholdResource -> createMembership(arbeidsforholdResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(Membership -> {
                    log.debug("Membership with memberid {} has status {}",
                            Membership.getMemberId(), Membership.getMemberStatus());
                } );
                //.distinct();
        return getUniqueMemberships(allMemberships);
    }
    public List<Membership> createAggregatedOrgUnitMembershipList(Role role, RoleService roleService) {
        List<Role> allRoles = new ArrayList<Role>();
        allRoles.add(role);

        List<Role> aggregatedRoles = role.getChildrenRoleIds()
                .stream()
                .map(RoleRef::getRoleRef)
                .map(roleService::getOptionalRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        aggregatedRoles.stream().forEach(aggrrole -> allRoles.add(aggrrole));

        Stream<Membership> allMemberships = allRoles
                .stream()
                .flatMap(aggrRole -> aggrRole.getMemberships().stream());

        return getUniqueMemberships(allMemberships);
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

    private Optional<Membership> createMembership(ArbeidsforholdResource arbeidsforholdResource, Date currentTime) {

        Optional<PersonalressursResource> personalressursResource = arbeidsforholdService.getPersonalressurs(arbeidsforholdResource);

        if (personalressursResource.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> user = userService.getUser(personalressursResource.get().getAnsattnummer().getIdentifikatorverdi());
        if (user.isEmpty()) {
            return Optional.empty();
        }
        String userStatus = user.get().getStatus();
        String membershipStatus = userStatus == null || userStatus.equals("ACTIVE") ?
                MembershipUtils.getMembershipStatus(arbeidsforholdResource, currentTime) :
                "INACTIVE";
//        Date membershipStatusDate = membershipStatus.equals("ACTIVE")
//                ? arbeidsforholdResource.getGyldighetsperiode().getStart()
//                : arbeidsforholdResource.getGyldighetsperiode().getSlutt();

        return Optional.of(CreateMembership(user.get(), membershipStatus));
    }

    private Membership CreateMembership(
            User user,
            String membershipStatus
            //Date membershipStatusDate
    ){
        return Membership.builder()
                .memberId(user.getId())
                .memberStatus(membershipStatus)
                //.memberStatusChanged(membershipStatusDate)
                .build();
    }
}
