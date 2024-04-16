package no.fintlabs.role;

import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.cache.FintCache;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UndervisningsgruppemedlemskapService {

    private final FintCache<String, UndervisningsgruppemedlemskapResource> undervisningsgruppemedlemskapResourceCache;

    public UndervisningsgruppemedlemskapService(FintCache<String, UndervisningsgruppemedlemskapResource> undervisningsgruppemedlemskapResourceCache) {
        this.undervisningsgruppemedlemskapResourceCache = undervisningsgruppemedlemskapResourceCache;
    }

    public Optional<UndervisningsgruppemedlemskapResource> getUndervisningsgruppemedlemskap (Link undervisningsgruppemedlemskapLink) {
        return undervisningsgruppemedlemskapResourceCache.getOptional(undervisningsgruppemedlemskapLink.getHref());
    }
}
