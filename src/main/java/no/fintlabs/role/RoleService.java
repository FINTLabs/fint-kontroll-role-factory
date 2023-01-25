package no.fintlabs.role;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RoleService {
    private final FintCache<String, Role> roleCache;
    private final OrganisasjonselementService organisasjonselementService;

    public RoleService(FintCache<String, Role> roleCache, OrganisasjonselementService organisasjonselementService) {
        this.roleCache = roleCache;
        this.organisasjonselementService = organisasjonselementService;
    }
    public String createRoleId(OrganisasjonselementResource organisasjonselementResource, String roleType, String subRoleType) {
        return roleType + "@" + organisasjonselementService.getNormalizedKortNavn(organisasjonselementResource);
    }
    public String createRoleName (String groupName, String roleType, String subRoleType)
    {
        return StringUtils.capitalize(roleType + " i " + subRoleType) + " " + groupName;
    }
}
