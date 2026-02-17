package no.fintlabs.termin;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.model.resource.Link;
import no.novari.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.novari.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.role.GyldighetsperiodeService;
import no.fintlabs.role.RoleStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class TerminService {
    private final GyldighetsperiodeService gyldighetsperiodeService;
    private final FintCache<String, TerminResource> terminResourceCache;

    public TerminService(
            GyldighetsperiodeService gyldighetsperiodeService,
            FintCache<String, TerminResource> terminResourceCache
    ) {
        this.gyldighetsperiodeService = gyldighetsperiodeService;
        this.terminResourceCache = terminResourceCache;
    }

// TODO Replace RoleUtils.getUndervisningsgruppeRoleStatus with this method

    public RoleStatus getUndervisningsgruppeRoleStatus(UndervisningsgruppeResource undervisningsgruppeResource, Date currentTime) {
        Optional<Collection<Link>> terminLinks = Optional.ofNullable(undervisningsgruppeResource.getTermin());

        if (terminLinks.isEmpty() || terminLinks.get().isEmpty()) {
            log.warn("No termin links found for undervisningsgruppe {}. Status for role is set to ACTIVE",
                    undervisningsgruppeResource.getSystemId().getIdentifikatorverdi()
            );
            return new RoleStatus("ACTIVE", null);
        }
        log.info("Undervisningsgruppe role for undervisningsgruppe {} has status {}",
                undervisningsgruppeResource.getSystemId().getIdentifikatorverdi(),
                getStatusFromTerminList(terminLinks.get(), currentTime)
        );
        return new RoleStatus(
                getStatusFromTerminList(terminLinks.get(), currentTime),
                getStatusChangedFromTerminList(terminLinks.get(), currentTime)
        );
    }

    //TODO decide if this method is needed, is commented out in UndervisningsgruppeService for now.
    public boolean hasValidPeriod(Collection<Link> terminLinks, Date currentTime) {
        Optional<TerminResource> validTerminResource= terminLinks
                .stream()
                .map(Link::getHref)
                .map(ResourceLinkUtil::idAttributeToLowerCase)
                .map(terminResourceCache::getOptional)
                .map(Optional::get)
                .filter(terminResource -> gyldighetsperiodeService.isValid(terminResource.getGyldighetsperiode(), currentTime))
                .findFirst();

        return validTerminResource.isPresent();
    }

    public String getStatusFromTerminList(Collection<Link> terminLinks, Date currentTime) {
        return hasValidPeriod(terminLinks, currentTime) ? "ACTIVE" : "INACTIVE";
    }

    public  Date getStatusChangedFromTerminList(Collection<Link> links, Date currentTime) {
        return links.stream()
                .map(Link::getHref)
                .map(ResourceLinkUtil::idAttributeToLowerCase)
                .map(terminResourceCache::getOptional)
                .map(Optional::get)
                .filter(terminResource -> gyldighetsperiodeService.isValid(terminResource.getGyldighetsperiode(), currentTime))
                .map(TerminResource::getGyldighetsperiode)
                .map(gyldighetsperiode -> gyldighetsperiodeService.isValid(gyldighetsperiode, currentTime) ? gyldighetsperiode.getStart() : gyldighetsperiode.getSlutt())
                .findFirst()
                .orElse(null);
    }
}

