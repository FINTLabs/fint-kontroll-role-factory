package no.fintlabs.membership;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.user.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class MembershipPublishingComponent {
    private final OrganisasjonselementService organisasjonselementService;
    private final AdmMembershipService admMembershipService;
    private final MembershipEntityProducerService membershipEntityProducerService;
    private final UserService userService;

    public MembershipPublishingComponent(
            OrganisasjonselementService organisasjonselementService,
            AdmMembershipService admMembershipService,
            MembershipEntityProducerService membershipEntityProducerService,
            UserService userService
    ) {
        this.organisasjonselementService = organisasjonselementService;
        this.admMembershipService = admMembershipService;
        this.membershipEntityProducerService = membershipEntityProducerService;
        this.userService = userService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.publishing.fixed-delay}"
    )
    public void publishMemberships() {
        Date currentTime = Date.from(Instant.now());

        Long noOfUsersInCache = userService.getNumberOfUsersInCache();
        log.info("Start collecting org unit memberships with {} users in the kontrolluser cache", noOfUsersInCache);

        List<Membership> memberships = organisasjonselementService.getAll()
                .stream()
                .map(organisasjonselementResource -> admMembershipService.createOrgUnitMembershipList(organisasjonselementResource, currentTime))
                .flatMap(Collection::stream)
                .toList();

        List<Membership> publishedMemberships = membershipEntityProducerService.publishChangedMemberships(memberships);

        log.info("Published {} of {} valid org unit memberships", publishedMemberships.size(), memberships.size());
    }
}
