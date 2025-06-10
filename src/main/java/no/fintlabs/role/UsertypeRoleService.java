package no.fintlabs.role;

import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.utils.RoleUtils;
import org.apache.kafka.common.protocol.types.Field;

import java.util.*;

public class UsertypeRoleService {
    private final RoleService roleService;
    private final FintCache<String, Role> roleCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;

    public UsertypeRoleService(RoleService roleService, FintCache<String, Role> roleCache, OrganisasjonselementService organisasjonselementService, FintCache<String, RoleCatalogRole> roleCatalogRoleCache) {
        this.roleService = roleService;
        this.roleCache = roleCache;
        this.organisasjonselementService = organisasjonselementService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
    }

    public List<Role> createUserTypeRoles() {

        List<Role> userTypeRoles = new ArrayList<>();

        OrganisasjonselementResource mainOrgUnit = organisasjonselementService.getMainOrganisasjonselement()
                .orElseThrow(() -> new IllegalStateException("Main organization unit not found"));

        for (RoleUserType roleUserType : RoleUserType.values()) {
            Role usertypeRole = createUsertypeRole(roleUserType, mainOrgUnit);
            userTypeRoles.add(usertypeRole);
        }
        return userTypeRoles;
    }

    public Role createUsertypeRole(RoleUserType roleUserType, OrganisasjonselementResource mainOrgUnit) {

        String roleType = roleUserType.toString().toLowerCase();
        String roleId = roleService.createRoleId(mainOrgUnit, roleType,null, false);

        String resourceId = ResourceLinkUtil.getFirstSelfLink(mainOrgUnit);
        String organisationUnitId = mainOrgUnit.getOrganisasjonsId().getIdentifikatorverdi();
        String orgunitName = mainOrgUnit.getNavn();

        return Role
                .builder()
                .roleId(roleId)
                .roleType(roleType)
                .resourceId(resourceId)
                .organisationUnitId(organisationUnitId)
                .organisationUnitName(orgunitName)
                .aggregatedRole(false)
                .build();
    }
}
