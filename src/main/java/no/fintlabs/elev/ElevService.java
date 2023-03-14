package no.fintlabs.elev;

import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fintlabs.cache.FintCache;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ElevService {
    private final FintCache<String, ElevResource> elevResourceCache;

    public ElevService(FintCache<String, ElevResource> elevResourceCache) {
        this.elevResourceCache = elevResourceCache;
    }

    public Optional<ElevResource> getElev(String elevResourceHref) {
        return elevResourceCache.getOptional(elevResourceHref);
    }
}
