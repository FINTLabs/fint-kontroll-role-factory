package no.fintlabs.termin;

import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.role.GyldighetsperiodeService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TerminService {
    private final GyldighetsperiodeService gyldighetsperiodeService;
    private final FintCache<String, TerminResource> terminResourceCache;

    public TerminService(
            GyldighetsperiodeService gyldighetsperiodeService,
            FintCache<String, TerminResource> terminResourceCache
    ) {
        this.gyldighetsperiodeService = gyldighetsperiodeService;
        this.terminResourceCache = terminResourceCache;
    }

    public boolean hasValidPeriod(Collection<Link> terminLinks, Date currentTime) {
        Optional<TerminResource> validTerminResource= terminLinks
                .stream()
                .map(Link::getHref)
                .map(ResourceLinkUtil::systemIdToLowerCase)
                .map(terminResourceCache::getOptional)
                .map(Optional::get)
                .filter(terminResource -> gyldighetsperiodeService.isValid(terminResource.getGyldighetsperiode(), currentTime))
                .findFirst();

        if (validTerminResource.isEmpty()) {
            return false;
        }
        else {
            return  true;
        }
    }
}

