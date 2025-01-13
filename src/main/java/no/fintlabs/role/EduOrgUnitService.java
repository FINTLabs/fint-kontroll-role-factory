package no.fintlabs.role;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EduOrgUnitService {
    private final FintCache<String, SkoleResource> skoleResourceFintCache;
    private final OrganisasjonselementService organisasjonselementService;

    public EduOrgUnitService(
            FintCache<String, SkoleResource> skoleResourceFintCache, OrganisasjonselementService organisasjonselementService
    ) {
        this.skoleResourceFintCache = skoleResourceFintCache;
        this.organisasjonselementService = organisasjonselementService;
    }

    public List<String> findAllEduOrgUnits() {
        Optional<List<SkoleResource>> skoleResources = Optional.ofNullable(skoleResourceFintCache.getAll());

        if (skoleResources.isEmpty()) {
            return null;
        }
        return skoleResources
                .map(resources -> resources.stream()
                        .map(SkoleResource::getOrganisasjon)
                        .map(list -> list.get(0).getHref())
                        .map(organisasjonselementService::getOrganisasjonselementResource)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(organisasjonselementService::getAllSubOrgUnits)
                        .flatMap(List::stream)
                        .map(orgunit -> orgunit.getOrganisasjonsId().getIdentifikatorverdi())
                        .toList())
                .orElseGet(() -> null
                );
    }
}
