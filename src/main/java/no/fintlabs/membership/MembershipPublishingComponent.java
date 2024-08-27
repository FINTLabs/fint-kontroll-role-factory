package no.fintlabs.membership;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
public class MembershipPublishingComponent {
    private final OrganisasjonselementService organisasjonselementService;
    private final AdmMembershipService admMembershipService;
    private final MembershipEntityProducerService membershipEntityProducerService;

    public MembershipPublishingComponent(OrganisasjonselementService organisasjonselementService, AdmMembershipService admMembershipService, MembershipEntityProducerService membershipEntityProducerService) {
        this.organisasjonselementService = organisasjonselementService;
        this.admMembershipService = admMembershipService;
        this.membershipEntityProducerService = membershipEntityProducerService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.publishing.fixed-delay}"
    )
    public void publishMemberships() {
        Date currentTime = Date.from(Instant.now());

        List<Membership> memberships = organisasjonselementService.getAllValid(currentTime)
                .stream()
                .map(organisasjonselementResource -> admMembershipService.createOrgUnitMembershipList(organisasjonselementResource, currentTime))
                .flatMap(Collection::stream)
                .toList();

        List<Membership> publishedMemberships = membershipEntityProducerService.publishChangedMemberships(memberships);

        log.info("Published {} of {} valid org unit memberships", publishedMemberships.size(), memberships.size());
    }
}
