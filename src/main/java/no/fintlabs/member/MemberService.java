package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResources;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.role.*;
import no.fintlabs.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MemberService {
    private final FintCache<String , Member> memberCache;
    private final FintCache<String , User> userCache;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;
    private final RoleService roleService;

    public MemberService(
            FintCache<String, Member> memberCache,
            FintCache<String, User> userCache, FintCache<String,
            PersonalressursResource> personalressursResourceCache,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService,
            RoleService roleService
    ) {
        this.memberCache = memberCache;
        this.userCache = userCache;
        this.personalressursResourceCache = personalressursResourceCache;
        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;;
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

        List<Member> members = resources.getContent()
                .stream()
                .map(PersonalressursResource::getAnsattnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(href -> getMember(href))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        log.info("Found {} members for org unit {} ({})", members.size()
            , organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()
            , organisasjonselementResource.getOrganisasjonsKode().getIdentifikatorverdi()
        );
        return members;
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

    private Optional<Member> getMember (String userId)
    {
        //return memberCache.getOptional(memberId);
        Optional<User> optionalUser =userCache.getOptional(userId);

        if (!optionalUser.isEmpty()) {
            return Optional.of(optionalUser.get().toMember());
        }
        return Optional.empty();

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

        return allRoles.stream()
                .flatMap(aggrRole -> aggrRole.getMembers().stream())
                .distinct()
                .toList();
    }
}
