package no.fintlabs.role;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final FintCache<String, Role> roleCache;
    private final OrganisasjonselementService organisasjonselementService;

    public RoleService(FintCache<String, Role> roleCache, OrganisasjonselementService organisasjonselementService) {
        this.roleCache = roleCache;
        this.organisasjonselementService = organisasjonselementService;
    }
    public String createRoleId(OrganisasjonselementResource organisasjonselementResource, String roleType, String subRoleType, Boolean isAggregated) {
        String idSuffix = isAggregated ? "_aggr": "";

        return roleType + "@" + organisasjonselementService.getNormalizedKortNavn(organisasjonselementResource) + idSuffix;
    }
    public String createRoleName (String groupName, String roleType, String subRoleType)
    {
        return StringUtils.capitalize(roleType + " i " + subRoleType) + " " + groupName;
    }
    public List<RoleRef> createSubRoleList (
            OrganisasjonselementResource organisasjonselementResource,
            String roleType,
            String subRoleType,
            Boolean isAggregated
    ) {
        if (organisasjonselementResource.getUnderordnet().isEmpty())
            return new ArrayList<RoleRef>();

        return organisasjonselementService.getAllSubOrgUnits(organisasjonselementResource)
                .stream()
//                .map(orgunit -> Optional.ofNullable(orgunit))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
                .map(orgunit -> createRoleId(orgunit, roleType, "" , isAggregated))
                .map(RoleRef::new)
                .toList();
    }
}
