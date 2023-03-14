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

        List<String> basisgrupperToPublish = Arrays.asList("ALL");

        List<Role> validBasisgruppeRoles = basisgruppeService.getAllValid(currentTime)
                .stream()
                .filter(basisgruppeResource -> basisgrupperToPublish
                        .contains(basisgruppeResource.getSystemId().getIdentifikatorverdi())
                        || basisgrupperToPublish.contains("ALL")
                )
                .filter(basisgruppeResource -> !basisgruppeResource.getElevforhold().isEmpty())
                .map(basisgruppeResource -> createOptionalBasisgruppeRole(basisgruppeResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

                List< Role > publishedBasisgruppeRoles = roleEntityProducerService.publishChangedRoles(validBasisgruppeRoles);

        log.info("Published {} of {} valid roles", publishedBasisgruppeRoles.size(), validBasisgruppeRoles.size());
        log.debug("Ids of published roles: {}",
                publishedBasisgruppeRoles.stream()
                        .map(Role::getResourceId)
                        .map(href -> href.substring(href.lastIndexOf("/") + 1))
                        .toList()
        );

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
                .toList();

        List< Role > publishedRoles = roleEntityProducerService.publishChangedRoles(validOrgUnitRoles);

        log.info("Published {} of {} valid org unit roles", publishedRoles.size(), validOrgUnitRoles.size());
        log.debug("Ids of published org unit roles: {}",
                publishedRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );

        List<Role> validAggrOrgUnitRoles = roleService.getAllNonAggregatedOrgUnitRoles()
                .stream()
                .filter(role -> organisasjonselementToPublish.contains(role.getResourceId())
                        || organisasjonselementToPublish.contains("ALL"))
                .filter(role -> !role.getChildrenRoleIds().isEmpty())
                .map(role -> createOptionalAggrOrgUnitRole(role))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List< Role > publishedAggrRoles = roleEntityProducerService.publishChangedRoles(validAggrOrgUnitRoles);

        log.info("Published {} of {} valid aggregated org unit roles", publishedAggrRoles.size(), validAggrOrgUnitRoles.size());
        log.debug("Ids of published aggregated org unit roles: {}",
                publishedAggrRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }

    private Optional<Role> createOptionalBasisgruppeRole(BasisgruppeResource basisgruppeResource) {
        Optional<List<Member>> members = Optional.ofNullable(memberService.createBasisgruppeMemberList(basisgruppeResource));

        return  Optional.of(
                    createBasisgruppeRole(basisgruppeResource,
                    members.get())
        );
    }
    private Role createBasisgruppeRole(
            BasisgruppeResource basisgruppeResource,
            List<Member> members
    ) {
        String groupName = basisgruppeResource.getNavn();
        String roleType = RoleType.ELEV.getRoleType();
        String subRoleType = RoleSubType.BASISGRUPPE.getRoleSubType();
        String roleId = roleService.createBasisgruppeRoleId(basisgruppeResource, roleType);

        return Role
                .builder()
                //.id(Long.valueOf(basisgruppeResource.getSystemId().getIdentifikatorverdi()))
                .roleId(roleId)
                .resourceId(ResourceLinkUtil.getFirstSelfLink(basisgruppeResource))
                .roleName(roleService.createRoleName(groupName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                .aggregatedRole(false)
                .members(members)
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
                .roleName(originatingRoleName + " - Aggregert")
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
