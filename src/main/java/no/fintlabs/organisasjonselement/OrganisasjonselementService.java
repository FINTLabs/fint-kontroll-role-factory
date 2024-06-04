
package no.fintlabs.organisasjonselement;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.role.*;
import no.fintlabs.cache.FintCache;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
                .peek(organisasjonselementResource -> {
                    if(!(gyldighetsperiodeService.isValid(organisasjonselementResource.getGyldighetsperiode(), currentTime))) {
                        log.info("Orgenhet {} ({}) er ikke aktiv, ingen ansattrolle blir derfor generert for denne enheten"
                            , organisasjonselementResource.getNavn()
                            , organisasjonselementResource.getOrganisasjonsKode()
                        );
                    }
                })
                .filter(organisasjonselementResource -> gyldighetsperiodeService.isValid(
                        organisasjonselementResource.getGyldighetsperiode(),
                        currentTime
                ))
                //.filter(organisasjonselementResource -> !organisasjonselementResource.getArbeidsforhold().isEmpty())
                .toList();
    }

    public List<ArbeidsforholdResource> getAllValidArbeidsforhold(
            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime
    ) {
        List<ArbeidsforholdResource> activeEmployments = organisasjonselementResource.getArbeidsforhold()
                .stream()
                .map(arbeidsforholdService::getArbeidsforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(arbeidsforholdResource -> gyldighetsperiodeService.isValid(arbeidsforholdResource.getGyldighetsperiode(), currentTime))
                .toList();

        List<String> activePersonalressursIds = activeEmployments
                .stream()
                .map(arbeidsforholdResource -> ResourceLinkUtil.getFirstLink(
                        arbeidsforholdResource::getPersonalressurs,
                        arbeidsforholdResource,
                        "Personalressurs"
                ))
                .map(ResourceLinkUtil::systemIdToLowerCase)
                .distinct()
                .toList();

        List<ArbeidsforholdResource> inactiveEmployments = organisasjonselementResource.getArbeidsforhold()
                .stream()
                .map(arbeidsforholdService::getArbeidsforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(arbeidsforholdResource -> !activePersonalressursIds.contains(ResourceLinkUtil.systemIdToLowerCase(
                        ResourceLinkUtil.getFirstLink(
                                arbeidsforholdResource::getPersonalressurs,
                                arbeidsforholdResource,
                                "Personalressurs"
                        )
                )))
                .toList();

        // return union of activeEmployments and inactiveEmployments
        return new ArrayList<>(activeEmployments) {{
            addAll(inactiveEmployments);
        }};

    }

    public List<OrganisasjonselementResource> getAllSubOrgUnits (OrganisasjonselementResource organisasjonselementResource) {

        String resourceId = ResourceLinkUtil.getFirstSelfLink(organisasjonselementResource);
        List<OrganisasjonselementResource> subOrgUnits = new ArrayList<>();
        collect(organisasjonselementResource, subOrgUnits);

        return subOrgUnits;
    }

    public List<OrganisasjonselementResource> getSubOrgUnitsThisOrgUnit (OrganisasjonselementResource organisasjonselementResource) {
        return getUnderOrdnetOrgUnits(organisasjonselementResource);
    }
    public Optional<OrganisasjonselementResource> getOrganisasjonsResource (SkoleResource skoleResource)
    {        return organisasjonselementResourceCache.getOptional(
            ResourceLinkUtil.getFirstLink(
                    skoleResource::getOrganisasjon,
                    skoleResource,
                    "Organisasjonselement"
            ));

    }

    private void collect(OrganisasjonselementResource orgUnit, List<OrganisasjonselementResource > subOrgUnits) {
        subOrgUnits.add(orgUnit);

        for (OrganisasjonselementResource subOrgUnit : getUnderOrdnetOrgUnits(orgUnit)) {
            collect(subOrgUnit, subOrgUnits);
        }
    }
    private List<OrganisasjonselementResource> getUnderOrdnetOrgUnits(OrganisasjonselementResource organisasjonselementResource){
        String resourceId = ResourceLinkUtil.getFirstSelfLink(organisasjonselementResource);
        return organisasjonselementResourceCache.get(ResourceLinkUtil.organisasjonsIdToLowerCase(resourceId))
                        .getUnderordnet()
                        .stream()
                        .map(Link::getHref)
                        .map(href -> organisasjonselementResourceCache.get(ResourceLinkUtil.organisasjonsIdToLowerCase(href)))
                        .toList();
    }
}

