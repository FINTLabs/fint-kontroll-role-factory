package no.fintlabs.role;

import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.apache.kafka.common.protocol.types.Field;

import java.util.*;

public class UsertypeRoleService {
    private final FintCache<String, Role> roleCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;

    public UsertypeRoleService(FintCache<String, Role> roleCache, OrganisasjonselementService organisasjonselementService, FintCache<String, RoleCatalogRole> roleCatalogRoleCache) {
        this.roleCache = roleCache;
        this.organisasjonselementService = organisasjonselementService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
    }

    public List<Role> createUsertypeRoles() {

        List<Role> userTypeRoles = new ArrayList<>();

        OrganisasjonselementResource mainOrgUnit = organisasjonselementService.getMainOrganisasjonselement()
                .orElseThrow(() -> new IllegalStateException("Main organization unit not found"));


        for (RoleUserType roleUserType : RoleUserType.values()) {
            ;
        }
        return userTypeRoles;
    }
}
