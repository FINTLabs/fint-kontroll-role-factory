package no.fintlabs.arbeidsforhold;

import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.role.GyldighetsperiodeService;
import no.fintlabs.personalressurs.PersonalressursService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArbeidsforholdService {

    private final GyldighetsperiodeService gyldighetsperiodeService;
    private final FintCache<String, ArbeidsforholdResource> arbeidsforholdResourceCache;
    private final FintCache<String, OrganisasjonselementResource> organisasjonselementResourceCache;
    private final PersonalressursService personalressursService;

    public ArbeidsforholdService(
            GyldighetsperiodeService gyldighetsperiodeService,
            FintCache<String, ArbeidsforholdResource> arbeidsforholdResourceCache,
            FintCache<String, OrganisasjonselementResource> organisasjonselementResourceCache,
            PersonalressursService personalressursService
    ) {
        this.gyldighetsperiodeService = gyldighetsperiodeService;
        this.arbeidsforholdResourceCache = arbeidsforholdResourceCache;
        this.organisasjonselementResourceCache = organisasjonselementResourceCache;
        this.personalressursService = personalressursService;
    }

    public Optional<ArbeidsforholdResource> getArbeidsforhold(Link arbeidsforholdLink) {
        return arbeidsforholdResourceCache.getOptional(ResourceLinkUtil.idAttributeToLowerCase(arbeidsforholdLink.getHref()));
    }


    private Optional<ArbeidsforholdResource> getValidMainArbeidsforhold(List<ArbeidsforholdResource> arbeidsforholdResources, Date currentTime) {
        return arbeidsforholdResources
                .stream()
                .filter(ArbeidsforholdResource::getHovedstilling)
                .filter(arbeidsforholdResource -> isValid(arbeidsforholdResource, currentTime))
                .findFirst();
    }

    private Optional<ArbeidsforholdResource> getValidNonMainArbeidsforhold(List<ArbeidsforholdResource> arbeidsforholdResources, Date currentTime) {
        return arbeidsforholdResources
                .stream()
                .filter(arbeidsforholdResource -> !arbeidsforholdResource.getHovedstilling())
                .filter(arbeidsforholdResource -> isValid(arbeidsforholdResource, currentTime))
                .max(Comparator.comparingLong(ArbeidsforholdResource::getAnsettelsesprosent));
    }

    private boolean isValid(ArbeidsforholdResource arbeidsforholdResource, Date currentTime) {
        return gyldighetsperiodeService.isValid(
                arbeidsforholdResource.getArbeidsforholdsperiode() != null
                        ? arbeidsforholdResource.getArbeidsforholdsperiode()
                        : arbeidsforholdResource.getGyldighetsperiode(),
                currentTime
        );
    }

    public Optional<PersonalressursResource> getPersonalressurs(ArbeidsforholdResource arbeidsforholdResource) {
        return personalressursService.getPersonalressurs(
                        ResourceLinkUtil.getFirstLink(arbeidsforholdResource::getPersonalressurs,
                        arbeidsforholdResource,
                        "personalressurs"));
    }
}
