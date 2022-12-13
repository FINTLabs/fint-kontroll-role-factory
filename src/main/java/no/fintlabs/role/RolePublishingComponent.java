package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.*;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.List;

@Slf4j
@Component
public class RolePublishingComponent {
    /*
    private final FintCache<String, ElevResource> elevResourceCache;
    private final FintCache<String, ElevforholdResource> elevforholdResourceFintCache;
    private final FintCache<String, SkoleressursResource> skoleressursResourceFintCache;
    private final FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache;
    private final FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache;
    */
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache;
    private final  FintCache<String, TerminResource> terminResourceCache;
    private final RoleEntityProducerService roleEntityProducerService;

    public RolePublishingComponent(
            /*
            FintCache<String, ElevResource> elevResourceCache,
            FintCache<String, ElevforholdResource> elevforholdResourceFintCache,
            FintCache<String, SkoleressursResource> skoleressursResourceFintCache,
            FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache,
            FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache,
                         */
            FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache,
            FintCache<String, TerminResource> terminResourceCache,
            RoleEntityProducerService roleEntityProducerService
    ) {
        /*
        this.elevResourceCache = elevResourceCache;
        this.elevforholdResourceFintCache = elevforholdResourceFintCache;
        this.skoleressursResourceFintCache = skoleressursResourceFintCache;
        this.undervisningsforholdResourceFintCache = undervisningsforholdResourceFintCache;
        this. basisgruppemedlemskapResourceFintCache = basisgruppemedlemskapResourceFintCache;
         */
        this.basisgruppeResourceFintCache = basisgruppeResourceFintCache;
        this.terminResourceCache = terminResourceCache;
        this.roleEntityProducerService = roleEntityProducerService;
    }

    @Scheduled(initialDelay = 5000L, fixedDelay = 3600000L)
        public void doSomething() {
        Date currentTime = Date.from(Instant.now());
        basisgruppeResourceFintCache.getAllDistinct()
                .stream()
                .filter(basisgruppeResource -> isTerminValid(basisgruppeResource.getTermin(), currentTime))
                .filter(basisgruppeResource-> !basisgruppeResource.getGruppemedlemskap().isEmpty())
                .filter(basisgruppeResource -> !basisgruppeResource.getSkole().isEmpty())
                .map(basisgruppeResource -> createRole(basisgruppeResource, currentTime))
        // TODO: 06/12/2022 For each:
        //  check if already published on event topic (compare hash)
        //  publish if not already published
                .forEach(role -> roleEntityProducerService.publish(role.get()));
    }

    private Optional<Role> createRole(BasisgruppeResource basisgruppeResource, Date currentTime) {
        //
        return  Optional.of(createRole(basisgruppeResource));
    }

    private Role createRole(BasisgruppeResource basisgruppeResource) {
        return Role
                .builder()
                .resourceId(ResourceLinkUtil.getFirstSelfLink(basisgruppeResource))
                .roleName(basisgruppeResource.getNavn())
                .roleType(RoleType.ELEVGRUPPE.getRoleType())
                .roleSubType(RoleSubType.BASISGRUPPE.getRoleSubType())
                .build();
    }

    private boolean isTerminValid(List<Link> termins, Date currentDate)
    {
        if (termins == null) {
            throw new NullTerminException();
        }
        //TODO rewrite as lambda?
        for (Link terminLink : termins )
        {
            Optional<TerminResource> terminResourceOptional = getTerminResource(terminLink.getHref());

            if (terminResourceOptional.isEmpty())
                return false;
            else {
                TerminResource termin = terminResourceOptional.get();

                if (isValid(termin.getGyldighetsperiode(), currentDate))
                    return true;
            }
        }
        return false;
    }
    private Optional<TerminResource> getTerminResource(String Id) {
        return terminResourceCache.getOptional(Id);
    }

    private boolean isValid(Periode periode, Date currentTime) {
        if (periode == null) {
            throw new NullPeriodeException();
        }
        return currentTime.after(periode.getStart())
                && isEndValid(periode.getSlutt(), currentTime);
    }

    private boolean isEndValid(Date end, Date currentTime) {
        return end == null || currentTime.before(end);
    }

    public static class NullPeriodeException extends RuntimeException {
    }
    public static class NullTerminException extends RuntimeException {
    }
}
