package no.fintlabs.role;

import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.termin.TerminService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BasisgruppeService {

    private final GyldighetsperiodeService gyldighetsperiodeService;
    private  final TerminService terminService;
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceCache;
    private final FintCache<String, TerminResource> terminResourceCache;

    public BasisgruppeService(
            GyldighetsperiodeService gyldighetsperiodeService,
            TerminService terminService,
            FintCache<String, BasisgruppeResource> basisgruppeResourceCache,
            FintCache<String, TerminResource> terminResourceCache
    ) {
        this.gyldighetsperiodeService = gyldighetsperiodeService;
        this.terminService = terminService;
        this.basisgruppeResourceCache = basisgruppeResourceCache;
        this.terminResourceCache = terminResourceCache;
    }

    public List<BasisgruppeResource> getAllValid(Date currentTime) {
        return basisgruppeResourceCache.getAllDistinct()
                .stream()
                .filter(basisgruppeResource -> terminService.hasValidPeriod(basisgruppeResource.getTermin(), currentTime))
                .toList();
    }

}