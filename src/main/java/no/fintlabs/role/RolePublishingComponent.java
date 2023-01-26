package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RolePublishingComponent {
    /*
    private final FintCache<String, ElevResource> elevResourceCache;
    private final FintCache<String, ElevforholdResource> elevforholdResourceFintCache;
    private final FintCache<String, SkoleressursResource> skoleressursResourceFintCache;
    private final FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache;

    */
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache;
    private final  FintCache<String, TerminResource> terminResourceCache;
    private final FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache;
    private final RoleEntityProducerService roleEntityProducerService;
    private final BasisgruppeService basisgruppeService;
    private final BasisgruppemedlemskapService basisgruppemedlemskapService;
    private final ElevforholdService elevforholdService;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;
    private  final SimpleMemberService simpleMemberService;
    private final MemberService memberService;
    private final RoleService roleService;

    //private final SkoleService skoleService;

    public RolePublishingComponent(
            /*
            FintCache<String, ElevResource> elevResourceCache,
            FintCache<String, ElevforholdResource> elevforholdResourceFintCache,
            FintCache<String, SkoleressursResource> skoleressursResourceFintCache,
            FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache,

            SkoleService skoleService,
             */
            FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache,
            FintCache<String, TerminResource> terminResourceCache,
            FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache,
            RoleEntityProducerService roleEntityProducerService,
            BasisgruppeService basisgruppeService,
            BasisgruppemedlemskapService basisgruppemedlemskapService,
            ElevforholdService elevforholdService,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService,
            SimpleMemberService simpleMemberService,
            MemberService memberService, RoleService roleService) {
        /*
        this.elevResourceCache = elevResourceCache;
        this.elevforholdResourceFintCache = elevforholdResourceFintCache;
        this.skoleressursResourceFintCache = skoleressursResourceFintCache;
        this.undervisningsforholdResourceFintCache = undervisningsforholdResourceFintCache;
         */
        this.basisgruppeResourceFintCache = basisgruppeResourceFintCache;
        this.terminResourceCache = terminResourceCache;
        this.basisgruppemedlemskapResourceFintCache = basisgruppemedlemskapResourceFintCache;
        this.roleEntityProducerService = roleEntityProducerService;
        //this.skoleService = skoleService;
        this.basisgruppeService = basisgruppeService;
        this.basisgruppemedlemskapService = basisgruppemedlemskapService;
        this.elevforholdService = elevforholdService;

        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
        this.simpleMemberService = simpleMemberService;

        this.memberService = memberService;
        this.roleService = roleService;
    }

    @Scheduled(
            //initialDelay = 20000L,
            //fixedDelay = 20000L
            initialDelayString = "${fint.kontroll.role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.publishing.fixed-delay}"
    )
        public void publishRoles() {
        Date currentTime = Date.from(Instant.now());
/*
        List<String> basisgrupperToPublish = Arrays.asList("");

        List<Role> validRoles = basisgruppeService.getAllValid(currentTime)
                .stream()
                .filter(basisgruppeResource -> basisgrupperToPublish.contains(basisgruppeResource.getSystemId().getIdentifikatorverdi()))
                .filter(basisgruppeResource -> !basisgruppeResource.getElevforhold().isEmpty())
                .map(basisgruppeResource -> createOptionalRole(basisgruppeResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

                List< Role > publishedRoles = roleEntityProducerService.publishChangedRoles(validRoles);

        log.info("Published {} of {} valid roles", publishedRoles.size(), validRoles.size());
        log.debug("Ids of published roles: {}",
                publishedRoles.stream()
                        .map(Role::getResourceId)
                        .map(href -> href.substring(href.lastIndexOf("/") + 1))
                        .toList()
        );*/

        //,"209","1009","915"
        List <String> organisasjonselementToPublish = Arrays.asList("36","38","46", "47", "48", "1178");

        List<Role> validOrgUnitRoles = organisasjonselementService.getAllValid(currentTime)
                .stream()
                .filter(organisasjonselementResource -> organisasjonselementToPublish.contains(organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()))
                .map(organisasjonselementResource -> createOptionalOrgUnitRole(organisasjonselementResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List< Role > publishedRoles = roleEntityProducerService.publishChangedRoles(validOrgUnitRoles);

        log.info("Published {} of {} valid org unit roles", publishedRoles.size(), validOrgUnitRoles.size());
        log.debug("Ids of published org unit roles: {}",
                publishedRoles.stream()
                        .map(Role::getResourceId)
                        .map(href -> href.substring(href.lastIndexOf("/") + 1))
                        .toList()
        );
        List<Role> validAggrOrgUnitRoles = organisasjonselementService.getAllValid(currentTime)
                .stream()
                .filter(organisasjonselementResource -> organisasjonselementToPublish.contains(organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()))
                .map(organisasjonselementResource -> createOptionalAggrOrgUnitRole(organisasjonselementResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List< Role > publishedAggrRoles = roleEntityProducerService.publishChangedRoles(validAggrOrgUnitRoles);

        log.info("Published {} of {} valid aggregated org unit roles", publishedAggrRoles.size(), validAggrOrgUnitRoles.size());
        log.debug("Ids of published org unit roles: {}",
                publishedAggrRoles.stream()
                        .map(Role::getResourceId)
                        .map(href -> href.substring(href.lastIndexOf("/") + 1))
                        .toList()
        );


    }

    private Optional<Role> createOptionalRole(BasisgruppeResource basisgruppeResource) {

        return  Optional.of(
                    createRole(basisgruppeResource)
        );
    }
    private Role createRole(
            BasisgruppeResource basisgruppeResource
    ) {
        String groupName = basisgruppeResource.getNavn();
        String roleType = RoleType.ELEV.getRoleType();
        String subRoleType = RoleSubType.BASISGRUPPE.getRoleSubType();
        List<SimpleMember> members = createMemberList(basisgruppeResource);

        return Role
                .builder()
                //.id(Long.valueOf(basisgruppeResource.getSystemId().getIdentifikatorverdi()))
                .resourceId(ResourceLinkUtil.getFirstSelfLink(basisgruppeResource))
                .roleName(roleService.createRoleName(groupName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                //.members(members)
                .build();
    }
    private Optional<Role> createOptionalOrgUnitRole(OrganisasjonselementResource organisasjonselementResource, Date currentTime) {
        String roleType = RoleType.ANSATT.getRoleType();
        String subRoleType = RoleSubType.ORGANISASJONSELEMENT.getRoleSubType();
        String roleId = roleService.createRoleId(organisasjonselementResource, roleType, subRoleType, false);

        Optional<List<Member>> members = Optional.ofNullable(memberService.createOrgUnitMemberList(organisasjonselementResource, currentTime));
        List<RoleRef> subRoles =roleService.createSubRoleList(organisasjonselementResource, roleType, subRoleType, false)
                .stream()
                .filter(roleRef -> !roleRef.getRoleRef().equalsIgnoreCase(roleId))
                .toList();
        return  Optional.of(
                createOrgUnitRole(
                        organisasjonselementResource,
                        roleType,
                        subRoleType,
                        roleId,
                        members.get(),
                        subRoles)
        );
    }
    private Role createOrgUnitRole(
            OrganisasjonselementResource organisasjonselementResource,
            String roleType,
            String subRoleType,
            String roleId,
            List<Member> members,
            List<RoleRef> subRoles
    ) {
        String resourceId = ResourceLinkUtil.getFirstSelfLink(organisasjonselementResource);
        String groupName = organisasjonselementResource.getNavn();


        return Role
                .builder()
                .resourceId(resourceId)
                .roleId(roleId)
                .roleName(roleService.createRoleName(groupName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                .members(members)
                .childrenRoleIds(subRoles)
                .build();
    }

    private Optional<Role> createOptionalAggrOrgUnitRole(OrganisasjonselementResource organisasjonselementResource, Date currentTime) {

        return  Optional.of(
                createAggrOrgUnitRole(organisasjonselementResource, currentTime)
        );
    }
    private Role createAggrOrgUnitRole (

            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime) {
        String groupName = organisasjonselementResource.getNavn();
        String roleType = RoleType.ANSATT.getRoleType();
        String subRoleType = RoleSubType.ORGANISASJONSELEMENT_AGGREGERT.getRoleSubType();
        List<Member> members = memberService.createOrgUnitMemberList(organisasjonselementResource, currentTime);

        return Role
                .builder()
                //.id(Long.valueOf(organisasjonselementResource.getSystemId().getIdentifikatorverdi()))
                .resourceId(ResourceLinkUtil.getFirstSelfLink(organisasjonselementResource))
                .roleId(roleService.createRoleId(organisasjonselementResource, roleType, subRoleType, true) )
                .roleName(roleService.createRoleName(groupName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                .members(members)
                .build();
    }

    private List<SimpleMember> createMemberList (BasisgruppeResource basisgruppeResource)
    {
        return basisgruppeService.getGruppemedlemskapHrefs(basisgruppeResource)
                .stream()
                .map(memberHref -> basisgruppemedlemskapService.getElevforholdHref(memberHref).get())
                .map(elevforholdHref -> elevforholdService.getElevHref(elevforholdHref).get())
                .map(href->href.substring(href.lastIndexOf("/") + 1))
                .map(Long::parseLong)
                .map(SimpleMember::new)
                .toList();
    }
}
