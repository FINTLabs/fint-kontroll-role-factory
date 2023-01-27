package no.fintlabs.member;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResources;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    private final FintCache<String , Member> memberCache;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;
    private final RoleService roleService;

    public MemberService(
            FintCache<String, Member> memberCache,
            FintCache<String, PersonalressursResource> personalressursResourceCache, OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService,
            RoleService roleService) {
        this.memberCache = memberCache;
        this.personalressursResourceCache = personalressursResourceCache;
        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
        this.roleService = roleService;
    }

    public List<Member> createOrgUnitMemberList (
            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime
    ) {
        PersonalressursResources resources = new PersonalressursResources();

        organisasjonselementService.getAllValidArbeidsforhold(organisasjonselementResource, currentTime)
                .stream()
                .map(arbeidsforholdResource -> arbeidsforholdService.getPersonalressurs(arbeidsforholdResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .forEach(resources::addResource);


        if (!organisasjonselementResource.getUnderordnet().isEmpty()) {
            getManagersThisSubUnit(organisasjonselementResource).forEach(resources::addResource);
        }

        return resources.getContent()
                .stream()
                .map(resource -> resource.getAnsattnummer().getIdentifikatorverdi())
                .map(href->href.substring(href.lastIndexOf("/") + 1))
                .map(employeeNumber -> getMember(employeeNumber))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private List<PersonalressursResource> getManagersThisSubUnit(OrganisasjonselementResource organisasjonselementResource) {
         return organisasjonselementService.getSubOrgUnitsThisOrgUnit(organisasjonselementResource)
                .stream()
                .map(arbeidssted -> ResourceLinkUtil.getOptionalFirstLink(arbeidssted::getLeder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(leder -> personalressursResourceCache.getOptional(leder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Member> getMember (String memberId)
    {
        return memberCache.getOptional(memberId);
    }

    public List<Member> createOrgUnitAggregatedMemberList(Role role) {
        List<Role> allRoles = new ArrayList<Role>();
        allRoles.add(role);

        List<Role> aggregatedRoles = role.getChildrenRoleIds()
                .stream()
                .map(roleRef -> roleRef.getRoleRef())
                .map(roleId -> roleService.getOptionalRole(roleId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        aggregatedRoles.stream().forEach(aggrrole -> allRoles.add(aggrrole));

        return aggregatedRoles.stream()
                .flatMap(aggrRole -> aggrRole.getMembers().stream())
                .distinct()
                .toList();
    }
}
