package no.fintlabs.role;

import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BasisgruppemedlemskapService {

    private final FintCache<String, BasisgruppemedlemskapResource> basisgruppemedlemskapResourceCache;

    public BasisgruppemedlemskapService(FintCache<String, BasisgruppemedlemskapResource> basisgruppemedlemskapResourceCache) {
        this.basisgruppemedlemskapResourceCache = basisgruppemedlemskapResourceCache;
    }

    public Optional<BasisgruppemedlemskapResource> getBasisgruppemedlemskap (Link basisgruppemedlemskapLink) {
        return basisgruppemedlemskapResourceCache.getOptional(basisgruppemedlemskapLink.getHref());
    }
}
