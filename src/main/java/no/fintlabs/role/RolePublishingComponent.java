package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class RolePublishingComponent {
    /*
    private final FintCache<String, ElevResource> elevResourceCache;
    private final FintCache<String, ElevforholdResource> elevforholdResourceFintCache;
    private final FintCache<String, SkoleressursResource> skoleressursResourceFintCache;
    private final FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache;
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache;
    private final  FintCache<String, TerminResource> terminResourceCache;
    private final FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache;
    private final BasisgruppeService basisgruppeService;
    private final BasisgruppemedlemskapService basisgruppemedlemskapService;
    private final ElevforholdService elevforholdService;
        private final SkoleService skoleService;
    */

    private final RoleEntityProducerService roleEntityProducerService;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;
    private  final SimpleMemberService simpleMemberService;
    private final MemberService memberService;
    private final RoleService roleService;

    public RolePublishingComponent(
            /*
            FintCache<String, ElevResource> elevResourceCache,
            FintCache<String, ElevforholdResource> elevforholdResourceFintCache,
            FintCache<String, SkoleressursResource> skoleressursResourceFintCache,
            FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache,
            SkoleService skoleService,
            FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache,
            FintCache<String, TerminResource> terminResourceCache,
            FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache,

            BasisgruppeService basisgruppeService,
            BasisgruppemedlemskapService basisgruppemedlemskapService,
            ElevforholdService elevforholdService,
            */
            RoleEntityProducerService roleEntityProducerService,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService,

            SimpleMemberService simpleMemberService,
            MemberService memberService, RoleService roleService) {
        /*
        this.elevResourceCache = elevResourceCache;
        this.elevforholdResourceFintCache = elevforholdResourceFintCache;
        this.skoleressursResourceFintCache = skoleressursResourceFintCache;
        this.undervisningsforholdResourceFintCache = undervisningsforholdResourceFintCache;
                this.basisgruppeResourceFintCache = basisgruppeResourceFintCache;
        this.terminResourceCache = terminResourceCache;
        this.basisgruppemedlemskapResourceFintCache = basisgruppemedlemskapResourceFintCache;

        this.skoleService = skoleService;
        this.basisgruppeService = basisgruppeService;
        this.basisgruppemedlemskapService = basisgruppemedlemskapService;
        this.elevforholdService = elevforholdService;
         */

        this.roleEntityProducerService = roleEntityProducerService;

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

//TODO move publish of school roles to separate class

//        List<String> skolerToPublish = Arrays.asList("ALL");
//
//        List<Role> validSkoleRoles = skoleService.getAll()
//                .stream()
//                .filter(skoleResource -> skolerToPublish.contains(skoleResource.getSkolenummer().getIdentifikatorverdi())
//                || skolerToPublish.contains("ALL"))
//                .filter(skoleResource -> !skoleResource.getElevforhold().isEmpty())
//                .map(skoleResource -> createOptionalSkoleRole(skoleResource, currentTime))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//        List<Role> publishedSkoleRoles = roleEntityProducerService.publishChangedRoles(validSkoleRoles);
//
//        log.info("Published {} of {} valid skole roles", publishedSkoleRoles.size(), validSkoleRoles.size());
//        log.debug("Ids of published basisgruppe roles: {}",
//                publishedSkoleRoles.stream()
//                        .map(Role::getRoleId)
//                        .toList()
//        );
//
//        List<String> basisgrupperToPublish = Arrays.asList("ALL");
//
//        List<Role> validBasisgruppeRoles = basisgruppeService.getAllValid(currentTime)
//                .stream()
//                .filter(basisgruppeResource -> basisgrupperToPublish
//                        .contains(basisgruppeResource.getSystemId().getIdentifikatorverdi())
//                        || basisgrupperToPublish.contains("ALL")
//                )
//                .filter(basisgruppeResource -> !basisgruppeResource.getElevforhold().isEmpty())
//                .map(basisgruppeResource -> createOptionalBasisgruppeRole(basisgruppeResource))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//
//        List< Role > publishedBasisgruppeRoles = roleEntityProducerService.publishChangedRoles(validBasisgruppeRoles);
//
//        log.info("Published {} of {} valid basisgruppe roles", publishedBasisgruppeRoles.size(), validBasisgruppeRoles.size());
//        log.debug("Ids of published basisgruppe roles: {}",
//                publishedBasisgruppeRoles.stream()
//                        .map(Role::getRoleId)
//                        .toList()
//        );

        //("36","38","40","42","43","46", "47", "48", "50","1163","378");
        List <String> organisasjonselementToPublish = Arrays.asList("ALL");

        List<Role> validOrgUnitRoles = organisasjonselementService.getAllValid(currentTime)
                .stream()
                .filter(organisasjonselementResource -> organisasjonselementToPublish
                        .contains(organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi())
                        || organisasjonselementToPublish.contains("ALL")
                )
                .map(organisasjonselementResource -> createOptionalOrgUnitRole(organisasjonselementResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                //.filter(role -> role.getMembers()!=null && !role.getMembers().isEmpty())
                .toList();

        List< Role > publishedRoles = roleEntityProducerService.publishChangedRoles(validOrgUnitRoles);

        log.info("Published {} of {} valid org unit roles", publishedRoles.size(), validOrgUnitRoles.size());
        log.info("Ids of published org unit roles: {}",
                publishedRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );

        List<Role> validAggrOrgUnitRoles = roleService.getAllNonAggregatedOrgUnitRoles()
                .stream()
                .filter(role -> organisasjonselementToPublish.contains(role.getResourceId())
                        || organisasjonselementToPublish.contains("ALL"))
                .filter(role -> role.getChildrenRoleIds() != null && !role.getChildrenRoleIds().isEmpty())
                .map(role -> createOptionalAggrOrgUnitRole(role))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List< Role > publishedAggrRoles = roleEntityProducerService.publishChangedRoles(validAggrOrgUnitRoles);

        log.info("Published {} of {} valid aggregated org unit roles", publishedAggrRoles.size(), validAggrOrgUnitRoles.size());
        log.info("Ids of published aggregated org unit roles: {}",
                publishedAggrRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }

//    private Optional<Role> createOptionalSkoleRole(SkoleResource skoleResource, Date currentTime) {
//        Optional<List<Member>> members = Optional.ofNullable(memberService.createSkoleMemberList(skoleResource, currentTime));
//        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(skoleResource);
//        return  Optional.of(
//                    createSkoleRole(skoleResource,
//                    organisasjonselementResource.get(),
//                    members.get())
//        );
//    }
//    private Role createSkoleRole (
//            SkoleResource skoleResource,
//            OrganisasjonselementResource organisasjonselementResource,
//            List<Member> members
//    ) {
//        String roleType = RoleType.ELEV.getRoleType();
//
//        return getEducationRole(
//                organisasjonselementResource,
//                members,
//                roleType,
//                skoleResource.getNavn(),
//                RoleSubType.SKOLEGRUPPE.getRoleSubType(),
//                roleService.createSkoleRoleId(skoleResource, roleType),
//                ResourceLinkUtil.getFirstSelfLink(skoleResource)
//        );
//    }
//    private Optional<Role> createOptionalBasisgruppeRole(BasisgruppeResource basisgruppeResource) {
//        Optional<List<Member>> members = Optional.ofNullable(memberService.createBasisgruppeMemberList(basisgruppeResource));
//        Optional<SkoleResource> optionalSkole = skoleService.getSkole(basisgruppeResource);
//        SkoleResource skoleResource = optionalSkole.get();
//        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(skoleResource);
//
//        return  Optional.of(
//                    createBasisgruppeRole(basisgruppeResource,
//                    organisasjonselementResource.get(),
//                    members.get())
//        );
//    }
//    private Role createBasisgruppeRole(
//            BasisgruppeResource basisgruppeResource,
//            OrganisasjonselementResource organisasjonselementResource,
//            List<Member> members
//    ) {
//        String roleType = RoleType.ELEV.getRoleType();
//
//        return getEducationRole(
//                organisasjonselementResource,
//                members,
//                roleType,
//                basisgruppeResource.getNavn(),
//                RoleSubType.BASISGRUPPE.getRoleSubType(),
//                roleService.createBasisgruppeRoleId(basisgruppeResource, roleType),
//                ResourceLinkUtil.getFirstSelfLink(basisgruppeResource)
//        );
//    }

//    private Role getEducationRole(
//            OrganisasjonselementResource organisasjonselementResource,
//            List<Member> members,
//            String roleType,
//            String groupName,
//            String subRoleType,
//            String roleId,
//            String selfLink
//    ) {
//        String organizationUnitId = organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi();
//        String organizationUnitName = organisasjonselementResource.getNavn();
//
//        return Role
//                .builder()
//                .roleId(roleId)
//                .resourceId(selfLink)
//                .roleName(roleService.createRoleName(groupName, roleType, subRoleType))
//                .roleSource(RoleSource.FINT.getRoleSource())
//                .roleType(roleType)
//                .roleSubType(subRoleType)
//                .aggregatedRole(false)
//                .organisationUnitId(organizationUnitId)
//                .organisationUnitName(organizationUnitName)
//                .members(members)
//                .build();
//    }

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
        String organisationUnitId = organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi();
        String orgunitName = organisasjonselementResource.getNavn();

        return Role
                .builder()
                .resourceId(resourceId)
                .roleId(roleId)
                .roleName(roleService.createRoleName(orgunitName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                .aggregatedRole(false)
                .organisationUnitId(organisationUnitId)
                .organisationUnitName(orgunitName)
                .members(members)
                .childrenRoleIds(subRoles)
                .build();
    }
    private Optional<Role> createOptionalAggrOrgUnitRole(Role role) {

        return  Optional.of(
                createAggrOrgUnitRole(role)
        );
    }
    private Role createAggrOrgUnitRole(Role role) {
        String originatingRoleName = role.getRoleName();
        String originatingRoleId = role.getRoleId();
        String roleType = RoleType.ANSATT.getRoleType();
        String subRoleType = RoleSubType.ORGANISASJONSELEMENT_AGGREGERT.getRoleSubType();
        List<Member> members = memberService.createOrgUnitAggregatedMemberList(role);

        return Role
                .builder()
                .resourceId(role.getResourceId())
                .roleId(originatingRoleId + "-aggr")
                .roleName(originatingRoleName + " - inkludert underenheter")
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(subRoleType)
                .aggregatedRole(true)
                .organisationUnitId(role.getOrganisationUnitId())
                .organisationUnitName(role.getOrganisationUnitName())
                .members(members)
                .build();
    }

}
