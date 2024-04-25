package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.elev.ElevService;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ElevforholdService {
    private final FintCache<String, ElevforholdResource> elevforholdResourceCache;
    private final ElevService elevService;
    private final GyldighetsperiodeService gyldighetsperiodeService;

    public ElevforholdService(
            FintCache<String, ElevforholdResource> elevforholdResourceCache,
            FintCache<String, ElevResource> elevResourceCache, ElevService elevService, GyldighetsperiodeService gyldighetsperiodeService) {
        this.elevforholdResourceCache = elevforholdResourceCache;
        this.elevService = elevService;
        this.gyldighetsperiodeService = gyldighetsperiodeService;
    }
    public Optional<ElevforholdResource> getElevforhold(BasisgruppemedlemskapResource basisgruppemedlemskapResource) {
        return elevforholdResourceCache.getOptional(
                ResourceLinkUtil.getFirstLink(
                        basisgruppemedlemskapResource::getElevforhold,
                        basisgruppemedlemskapResource,
                        "elevforhold"
                ));
    }
    public Optional<ElevforholdResource> getElevforhold(UndervisningsgruppemedlemskapResource UndervisningsgruppemedlemskapResource) {
        return elevforholdResourceCache.getOptional(
                ResourceLinkUtil.getFirstLink(
                        UndervisningsgruppemedlemskapResource::getElevforhold,
                        UndervisningsgruppemedlemskapResource,
                        "elevforhold"
                ));
    }

    public Optional<String> getElevHref (String elevforholdHref)
    {
        Optional<ElevforholdResource> elevforholdResourceOptional =
                elevforholdResourceCache.getOptional(elevforholdHref);

        if (elevforholdResourceOptional.isEmpty())
        {
            return Optional.empty();
        }
        return elevforholdResourceOptional.get().getElev()
                .stream()
                .findFirst()
                .map(Link::getHref)
                .map(ResourceLinkUtil::systemIdToLowerCase);
    }

    public Optional<ElevResource> getElev(ElevforholdResource elevforholdResource) {
        return elevService.getElev(
                ResourceLinkUtil.getFirstLink(elevforholdResource::getElev,
                        elevforholdResource,
                "elev")
        );
    }
    public Optional<ElevforholdResource> getElevforhold(Link elevforholdLink) {
        return elevforholdResourceCache.getOptional(ResourceLinkUtil.systemIdToLowerCase(elevforholdLink.getHref()));
    }

}
