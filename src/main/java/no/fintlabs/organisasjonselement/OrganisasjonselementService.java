
package no.fintlabs.organisasjonselement;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.role.GyldighetsperiodeService;
import no.fintlabs.cache.FintCache;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service

public class OrganisasjonselementService {
    private final FintCache<String, OrganisasjonselementResource> organisasjonselementResourceCache;
    private final GyldighetsperiodeService gyldighetsperiodeService;
    private final ArbeidsforholdService arbeidsforholdService;

    public OrganisasjonselementService(
            GyldighetsperiodeService gyldighetsperiodeService,
            FintCache<String, OrganisasjonselementResource> organisasjonselementResourceCache,
            ArbeidsforholdService arbeidsforholdService) {
        this.gyldighetsperiodeService = gyldighetsperiodeService;
        this.organisasjonselementResourceCache = organisasjonselementResourceCache;
        this.arbeidsforholdService = arbeidsforholdService;
    }

    public List<OrganisasjonselementResource> getAllValid(Date currentTime) {
        return organisasjonselementResourceCache.getAllDistinct()
                .stream()
                .filter(organisasjonselementResource -> gyldighetsperiodeService.isValid(
                        organisasjonselementResource.getGyldighetsperiode(),
                        currentTime
                ))
                .filter(organisasjonselementResource -> !organisasjonselementResource.getArbeidsforhold().isEmpty())
                .toList();
    }

    public List<ArbeidsforholdResource> getAllValidArbeidsforhold(
            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime
    ) {
        return organisasjonselementResource.getArbeidsforhold()
                .stream()
                .map(arbeidsforholdLink->arbeidsforholdService.getArbeidsforhold(arbeidsforholdLink))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(arbeidsforholdResource -> gyldighetsperiodeService.isValid(arbeidsforholdResource.getGyldighetsperiode(), currentTime))
                .toList();
    }
}
