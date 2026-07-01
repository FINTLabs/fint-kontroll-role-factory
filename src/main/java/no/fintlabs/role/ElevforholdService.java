package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import no.fint.model.resource.Link;

import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.elev.ElevService;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ElevforholdService {
    private final FintCache<String, ElevforholdResource> elevforholdResourceCache;
    private final ElevService elevService;

    public Optional<ElevforholdResource> getElevforhold(UndervisningsgruppemedlemskapResource UndervisningsgruppemedlemskapResource) {
        return elevforholdResourceCache.getOptional(
                ResourceLinkUtil.getFirstLink(
                        UndervisningsgruppemedlemskapResource::getElevforhold,
                        UndervisningsgruppemedlemskapResource,
                        "elevforhold"
                ));
    }
    public Optional<ElevResource> getElev(ElevforholdResource elevforholdResource) {
        return elevService.getElev(
                ResourceLinkUtil.getFirstLink(elevforholdResource::getElev,
                        elevforholdResource,
                "elev")
        );
    }
    public Optional<ElevforholdResource> getElevforhold(Link elevforholdLink) {
        return elevforholdResourceCache.getOptional(ResourceLinkUtil.idAttributeToLowerCase(elevforholdLink.getHref()));
    }

}
