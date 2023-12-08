package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RoleService {
    private final FintCache<String, Role> roleCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final MemberService memberService;

    public RoleService(FintCache<String, Role> roleCache, OrganisasjonselementService organisasjonselementService, MemberService memberService) {
        this.roleCache = roleCache;
        this.organisasjonselementService = organisasjonselementService;
        this.memberService = memberService;
    }

    public Optional<Role> createOptionalOrgUnitRole(OrganisasjonselementResource organisasjonselementResource, Date currentTime) {
        String roleType = RoleType.ANSATT.getRoleType();
        String subRoleType = RoleSubType.ORGANISASJONSELEMENT.getRoleSubType();
        String roleId = createRoleId(organisasjonselementResource, roleType, subRoleType, false);

        Optional<List<Member>> members = Optional.ofNullable(memberService.createOrgUnitMemberList(organisasjonselementResource, currentTime));
        List<RoleRef> subRoles =createSubRoleList(organisasjonselementResource, roleType, subRoleType, false)
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
                .roleName(createRoleName(orgunitName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                .aggregatedRole(false)
                .organisationUnitId(organisationUnitId)
                .organisationUnitName(orgunitName)
                .members(members)
                .noOfMembers(members.size())
                .childrenRoleIds(subRoles)
                .build();
    }
    public Optional<Role> createOptionalAggrOrgUnitRole(Role role) {

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
                .noOfMembers(members.size())
                .build();
    }
    public List<Role> getAllNonAggregatedOrgUnitRoles() {
        return roleCache.getAllDistinct()
                .stream()
                .filter(role -> !role.getAggregatedRole())
                //.filter(role -> role.getRoleType()=="ansatt")
                .toList();
    }
    public Optional<Role> getOptionalRole (String roleId) {
        return roleCache.getOptional(roleId);
    }

    public String createRoleId(OrganisasjonselementResource organisasjonselementResource, String roleType, String subRoleType, Boolean isAggregated) {
        String idSuffix = isAggregated ? "_aggr": "";

        return roleType + "@" + organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi() + idSuffix;
    }
    public String createBasisgruppeRoleId(BasisgruppeResource basisgruppeResource, String roleType)
    {
        String schoolHref =basisgruppeResource.getSkole().get(0).getHref();
        String schoolNumber =schoolHref.substring(schoolHref.lastIndexOf("/") + 1);
        String groupName = basisgruppeResource.getNavn();
        return roleType + "@" + schoolNumber + "-" + groupName;
    }
    public String createSkoleRoleId(SkoleResource skoleResource, String roleType)
    {
        String schoolNumber = skoleResource.getSkolenummer().getIdentifikatorverdi();
        return roleType + "@" + schoolNumber;
    }
    public String createRoleName (String groupName, String roleType, String subRoleType)
    {
        return StringUtils.capitalize(roleType) + " - " + groupName;
    }
    public List<RoleRef> createSubRoleList (
            OrganisasjonselementResource organisasjonselementResource,
            String roleType,
            String subRoleType,
            Boolean isAggregated
    ) {
        if (organisasjonselementResource.getUnderordnet().isEmpty())
            return new ArrayList<RoleRef>();

        log.info("Getting all suborgunits for org unit {} ({})"
                , organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()
                , organisasjonselementResource.getOrganisasjonsKode().getIdentifikatorverdi());
        List<RoleRef> allSubOrgUnitRefs = organisasjonselementService.getAllSubOrgUnits(organisasjonselementResource)
                .stream()
//                .map(orgunit -> Optional.ofNullable(orgunit))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
                .peek(orgunit->{log.info("Found sub org unit {} ({})"
                        ,orgunit.getOrganisasjonsId().getIdentifikatorverdi()
                        ,orgunit.getOrganisasjonsKode().getIdentifikatorverdi());})
                .map(orgunit -> createRoleId(orgunit, roleType, "" , isAggregated))
                .map(RoleRef::new)
                .toList();
        log.info("Found {} sub org units for orgunit {} ({})"
                , allSubOrgUnitRefs.size()
                , organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()
                , organisasjonselementResource.getOrganisasjonsKode().getIdentifikatorverdi()
        );
        return allSubOrgUnitRefs;
    }
}
