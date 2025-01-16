package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
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
        Optional<List<SkoleResource>> skoleResources = Optional.ofNullable(skoleResourceFintCache.getAllDistinct());

        if (skoleResources.isEmpty()) {
            return null;
        }
        List<String> schoolNames = skoleResources.get().stream()
                .map(SkoleResource::getNavn)
                .toList();
        log.info("Found {} skole resources {}", skoleResources.get().size(), schoolNames);

        return skoleResources
                .map(resources -> resources.stream()
                        .map(SkoleResource::getOrganisasjon)
                        .filter(list -> list != null && !list.isEmpty())
                        .map(list -> ResourceLinkUtil.idAttributeToLowerCase(list.get(0).getHref())                     )
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
