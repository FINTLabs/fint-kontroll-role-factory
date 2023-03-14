package no.fintlabs.role;

import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.elev.ElevService;
import no.fintlabs.links.ResourceLinkUtil;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ElevforholdService {
    private final FintCache<String, ElevforholdResource> elevforholdResourceCache;
    private final ElevService elevService;

    public ElevforholdService(
            FintCache<String, ElevforholdResource> elevforholdResourceCache,
            FintCache<String, ElevResource> elevResourceCache, ElevService elevService) {
        this.elevforholdResourceCache = elevforholdResourceCache;
        this.elevService = elevService;
    }
    public Optional<ElevforholdResource> getElevforhold(BasisgruppemedlemskapResource basisgruppemedlemskapResource) {
        return elevforholdResourceCache.getOptional(
                ResourceLinkUtil.getFirstLink(
                        basisgruppemedlemskapResource::getElevforhold,
                        basisgruppemedlemskapResource,
                        "elevforhold"
                ));
    }

    public Optional<ElevforholdResource> getElevforhold(Link elevforholdLink) {
        return elevforholdResourceCache.getOptional(ResourceLinkUtil.systemIdToLowerCase(elevforholdLink.getHref()));
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
}
