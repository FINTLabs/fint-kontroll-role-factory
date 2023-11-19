package no.fintlabs.role;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
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
