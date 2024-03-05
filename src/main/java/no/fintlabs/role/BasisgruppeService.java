package no.fintlabs.role;

import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.termin.TerminService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BasisgruppeService {

    private final ElevforholdService elevforholdService;
    private final BasisgruppemedlemskapService basisgruppemedlemskapService;
    private final GyldighetsperiodeService gyldighetsperiodeService;
    private  final TerminService terminService;
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceCache;
    private final FintCache<String, TerminResource> terminResourceCache;


    public BasisgruppeService(
            ElevforholdService elevforholdService, BasisgruppemedlemskapService basisgruppemedlemskapService, GyldighetsperiodeService gyldighetsperiodeService,
            TerminService terminService,
            FintCache<String, BasisgruppeResource> basisgruppeResourceCache,
            FintCache<String, TerminResource> terminResourceCache
            ) {
        this.elevforholdService = elevforholdService;
        this.basisgruppemedlemskapService = basisgruppemedlemskapService;
        this.gyldighetsperiodeService = gyldighetsperiodeService;
        this.terminService = terminService;
        this.basisgruppeResourceCache = basisgruppeResourceCache;
        this.terminResourceCache = terminResourceCache;
    }

    public List<BasisgruppeResource> getAllValid(Date currentTime) {
        return basisgruppeResourceCache.getAllDistinct()
                .stream()
                // TODO Add termin testdata
                //.filter(basisgruppeResource -> terminService.hasValidPeriod(basisgruppeResource.getTermin(), currentTime))
                .toList();
    }
    public List<ElevforholdResource> getAllElevforhold(
            BasisgruppeResource basisgruppeResource,
            Date currentTime
            ) {
        return basisgruppeResource.getGruppemedlemskap()
                .stream()
                .map(basisgruppemedlemskapService::getBasisgruppemedlemskap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(basisgruppemedlemskapResource ->
                        basisgruppemedlemskapResource.getGyldighetsperiode()==null||gyldighetsperiodeService.isValid(basisgruppemedlemskapResource.getGyldighetsperiode(),currentTime))
                .map(elevforholdService::getElevforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}