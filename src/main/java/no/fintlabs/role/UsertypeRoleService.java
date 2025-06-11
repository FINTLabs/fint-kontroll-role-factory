package no.fintlabs.role;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
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
            Role usertypeRole = createUserTypeRole(roleUserType, mainOrgUnit);
            userTypeRoles.add(usertypeRole);
        }
        return userTypeRoles;
    }

    public Role createUserTypeRole(RoleUserType roleUserType, OrganisasjonselementResource mainOrgUnit) {

        String roleType = roleUserType.toString();
        String roleId = roleService.createRoleId(mainOrgUnit, roleType.toLowerCase(),null, false);

        String resourceId = ResourceLinkUtil.getFirstSelfLink(mainOrgUnit);
        String organisationUnitId = mainOrgUnit.getOrganisasjonsId().getIdentifikatorverdi();
        String orgunitName = mainOrgUnit.getNavn();
        String roleName = createRoleName(roleUserType, orgunitName);

        return Role
                .builder()
                .roleId(roleId)
                .roleName(roleName)
                .roleType(roleType)
                .resourceId(resourceId)
                .organisationUnitId(organisationUnitId)
                .organisationUnitName(orgunitName)
                .aggregatedRole(false)
                .roleStatus("ACTIVE")
                .build();
    }

    private String createRoleName(RoleUserType roleUserType, String mainOrgUnitName) {

        switch (roleUserType) {
            case RoleUserType.STUDENT:
                return "Alle elever - " + mainOrgUnitName;
            case RoleUserType.EMPLOYEEFACULTY:
                return "Alle ansatte på skole - " + mainOrgUnitName;
            case RoleUserType.EMPLOYEESTAFF:
                return "Alle ansatte utenom skole - " + mainOrgUnitName;
        }
        throw new IllegalArgumentException("Unknown role user type: " + roleUserType);
    }
}
