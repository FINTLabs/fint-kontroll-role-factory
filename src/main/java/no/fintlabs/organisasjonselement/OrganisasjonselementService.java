
package no.fintlabs.organisasjonselement;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.role.GyldighetsperiodeService;
import no.fintlabs.cache.FintCache;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static no.fintlabs.utils.StringNormalizer.normalize;

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

    public List<String> getAllSubOrgUnits (OrganisasjonselementResource organisasjonselementResource) {

        String resourceId = ResourceLinkUtil.getFirstSelfLink(organisasjonselementResource);
        List<String> subOrgUnitHrefs = new ArrayList<>();
        collect(resourceId, subOrgUnitHrefs);

        return  subOrgUnitHrefs;

    }
    private void collect(String orgUnitHref, List<String > subOrgUnitHrefs) {
        subOrgUnitHrefs.add(orgUnitHref);

        for (String href : getUnderOrdnetHrefs(orgUnitHref)) {
            collect(href, subOrgUnitHrefs);
        }
    }
    private List<String> getUnderOrdnetHrefs(String orgUnitHref){
        return organisasjonselementResourceCache.get(ResourceLinkUtil.organisasjonsIdToLowerCase(orgUnitHref))
                        .getUnderordnet()
                        .stream()
                        .map(link -> link.getHref())
//                        .map(href -> organisasjonselementResourceCache.get(ResourceLinkUtil.organisasjonsIdToLowerCase(href)))
//                        .map(orgunit -> getNormalizedKortNavn(orgunit))
                        .toList();
    }

    public String getNormalizedKortNavn(OrganisasjonselementResource organisasjonselementResource) {
        return normalize(organisasjonselementResource.getKortnavn());
    }
}

