package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.utils.RoleUtils;
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
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;

    public RoleService(FintCache<String, Role> roleCache, OrganisasjonselementService organisasjonselementService, MemberService memberService, FintCache<String, RoleCatalogRole> roleCatalogRoleCache) {
        this.roleCache = roleCache;
        this.organisasjonselementService = organisasjonselementService;
        this.memberService = memberService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
    }

    public Optional<Role> createOptionalOrgUnitRole(OrganisasjonselementResource organisasjonselementResource, Date currentTime) {
        String roleType = RoleType.ANSATT.getRoleType();
        String subRoleType = RoleSubType.ORGANISASJONSELEMENT.getRoleSubType();
        String roleId = createRoleId(organisasjonselementResource, roleType, subRoleType, false);

        //Optional<List<Member>> members = Optional.ofNullable(memberService.createOrgUnitMemberList(organisasjonselementResource, currentTime));
        List<RoleRef> subRoles =createSubRoleList(organisasjonselementResource, roleType, subRoleType, false)
                .stream()
                //.filter(roleRef -> !roleRef.getRoleRef().equalsIgnoreCase(roleId))
                .toList();
        return  Optional.of(
                createOrgUnitRole(
                        currentTime,
                        organisasjonselementResource,
                        roleType,
                        subRoleType,
                        roleId,
                        //members.get(),
                        subRoles)
        );
    }
    private Role createOrgUnitRole(
            Date currentTime,
            OrganisasjonselementResource organisasjonselementResource,
            String roleType,
            String subRoleType,
            String roleId,
            //List<Member> members,
            List<RoleRef> subRoles
    ) {
        String resourceId = ResourceLinkUtil.getFirstSelfLink(organisasjonselementResource);
        String organisationUnitId = organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi();
        String orgunitName = organisasjonselementResource.getNavn();
        RoleStatus roleStatus = RoleUtils.getOrgUnitRoleStatus(organisasjonselementResource, currentTime);

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
                //.members(members)
                //.noOfMembers(members.size())
                .childrenRoleIds(subRoles)
                .roleStatus(roleStatus.status())
                .roleStatusChanged(roleStatus.statusChanged())
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
        List<RoleRef> childrenRoleIds = role.getChildrenRoleIds();
        //List<Member> members = createOrgUnitAggregatedMemberList(role);

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
                .childrenRoleIds(childrenRoleIds)
                //.members(members)
                //.noOfMembers(members.size())
                .roleStatus(role.getRoleStatus())
                .roleStatusChanged(role.getRoleStatusChanged())
                .build();
    }
//    private List<Member> createOrgUnitAggregatedMemberList(Role role) {
//        List<Role> allRoles = new ArrayList<Role>();
//        allRoles.add(role);
//
//        List<Role> aggregatedRoles = role.getChildrenRoleIds()
//                .stream()
//                .map(roleRef -> roleRef.getRoleRef())
//                .map(roleId -> getOptionalRole(roleId))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//
//        aggregatedRoles.stream().forEach(aggrrole -> allRoles.add(aggrrole));
//
//        return allRoles.stream()
//                .flatMap(aggrRole -> aggrRole.getMembers().stream())
//                .distinct()
//                .toList();
//    }
    public List<Role> getAllNonAggregatedOrgUnitRoles() {
        return roleCache.getAllDistinct()
                .stream()
                .filter(role -> !role.getAggregatedRole())
                //.filter(role -> role.getRoleType()=="ansatt")
                .toList();
    }
    public List<Role> getAllAggregatedOrgUnitRoles() {
        return roleCache.getAllDistinct()
                .stream()
                .filter(Role::getAggregatedRole)
                //.peek(role -> log.info("Found aggregated role {} with {} members", role.getRoleId(), role.getNoOfMembers()))
                .toList();
    }
    public Optional<Role> getOptionalRole (String roleId) {
        return roleCache.getOptional(roleId);
    }

    public String createRoleId(
            OrganisasjonselementResource organisasjonselementResource,
            String roleType,
            String subRoleType,
            Boolean isAggregated
    ) {
        String idSuffix = isAggregated ? "_aggr": "";
        return roleType + "@" + organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi() + idSuffix;
    }
    public String createUndervisningsgruppeRoleId(UndervisningsgruppeResource undervisningsgruppeResource, String roleType)
    {
        String schoolHref =undervisningsgruppeResource.getSkole().get(0).getHref();
        String schoolNumber =schoolHref.substring(schoolHref.lastIndexOf("/") + 1);
        String groupName = undervisningsgruppeResource.getNavn();
        return roleType + "@" + schoolNumber + "-" + groupName;
    }
    public String createSkoleRoleId(SkoleResource skoleResource, String roleType)
    {
        String schoolNumber = skoleResource.getSkolenummer().getIdentifikatorverdi();
        return roleType + "@" + schoolNumber;
    }
    public String createRoleName (String groupName, String roleType, String subRoleType)
    {
        if (subRoleType.equals("skolegruppe"))
            return "Alle elever - " + groupName;

        return StringUtils.capitalize(roleType) + " - " + groupName;
    }
    public String createSchoolRoleName (String groupName, String schoolShortName, String roleType, String subRoleType)
    {
        String prefix = StringUtils.capitalize(roleType) + " - " + schoolShortName + " ";

        if (subRoleType.equals("skolegruppe"))
            return prefix + " Alle elever " + groupName;

        return prefix + groupName;
    }
    public List<RoleRef> createSubRoleList (
            OrganisasjonselementResource organisasjonselementResource,
            String roleType,
            String subRoleType,
            Boolean isAggregated
    ) {
        if (organisasjonselementResource.getUnderordnet().isEmpty())
            return new ArrayList<RoleRef>();

        log.debug("Getting all suborgunits for org unit {} ({})"
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
        log.debug("Found {} sub org units for orgunit {} ({})"
                , allSubOrgUnitRefs.size()
                , organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()
                , organisasjonselementResource.getOrganisasjonsKode().getIdentifikatorverdi()
        );
        return allSubOrgUnitRefs;
    }

    public Optional<RoleCatalogRole> getRoleCatalogRole(String roleId) {
        return roleCatalogRoleCache.getOptional(roleId);
    }
}
