package no.fintlabs.role;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SkoleService {

    private final FintCache<String, SkoleResource> skoleResourceCache;
    private final ElevforholdService elevforholdService;
    private final GyldighetsperiodeService gyldighetsperiodeService;

    public SkoleService(FintCache<String, SkoleResource> skoleResourceCache, ElevforholdService elevforholdService, GyldighetsperiodeService gyldighetsperiodeService) {
        this.skoleResourceCache = skoleResourceCache;
        this.elevforholdService = elevforholdService;
        this.gyldighetsperiodeService = gyldighetsperiodeService;
    }

    public List<SkoleResource> getAll()
    {
        return skoleResourceCache.getAllDistinct();
    }

    public Optional<SkoleResource> getSkole(BasisgruppeResource basisgruppeResource) {
        return skoleResourceCache.getOptional(
                ResourceLinkUtil.getFirstLink(
                        basisgruppeResource::getSkole,
                        basisgruppeResource,
                        "Skole"
                ));
    }
    public List<ElevforholdResource> getAllValidElevforhold(SkoleResource skoleResource, Date currentTime) {
        return skoleResource.getElevforhold()
                .stream()
                .map(elevforholdService::getElevforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(elevforholdResource -> gyldighetsperiodeService.isValid(elevforholdResource.getGyldighetsperiode(), currentTime))
                .toList();
    }
}
