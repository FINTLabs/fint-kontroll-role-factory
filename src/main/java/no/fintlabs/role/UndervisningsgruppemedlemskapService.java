package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.cache.FintCache;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UndervisningsgruppemedlemskapService {

    private final FintCache<String, UndervisningsgruppemedlemskapResource> undervisningsgruppemedlemskapResourceCache;

    public Optional<UndervisningsgruppemedlemskapResource> getUndervisningsgruppemedlemskap (Link undervisningsgruppemedlemskapLink) {
        return undervisningsgruppemedlemskapResourceCache.getOptional(undervisningsgruppemedlemskapLink.getHref());
    }
}
