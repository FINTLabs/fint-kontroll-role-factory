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
    private final RoleEntityProducerService roleEntityProducerService;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;
    private  final SimpleMemberService simpleMemberService;
    private final MemberService memberService;
    private final RoleService roleService;

    public RolePublishingComponent(

            RoleEntityProducerService roleEntityProducerService,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService,

            SimpleMemberService simpleMemberService,
            MemberService memberService, RoleService roleService) {


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
