package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.SkoleService;
import no.fintlabs.role.UndervisningsgruppeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Collection;

@Slf4j
@Component
public class EduMembershipPublishingComponent {
    private final SkoleService skoleService;
    private final UndervisningsgruppeService undervisningsgruppeService;
    private final EduMembershipService eduMembershipService;
    private final MembershipEntityProducerService membershipEntityProducerService
            ;

    public EduMembershipPublishingComponent(SkoleService skoleService, UndervisningsgruppeService undervisningsgruppeService, EduMembershipService eduMembershipService, MembershipEntityProducerService membershipEntityProducerService) {
        this.skoleService = skoleService;
        this.undervisningsgruppeService = undervisningsgruppeService;
        this.eduMembershipService = eduMembershipService;
        this.membershipEntityProducerService = membershipEntityProducerService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.role.edu-publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.edu-publishing.fixed-delay}"
    )
    public void publishEduRoles() {
        Date currentTime = Date.from(Instant.now());

        List<Membership> skoleMemberships = skoleService.getAll()
                .stream()
                .map(skoleResource -> eduMembershipService.createSkoleMembershipList (skoleResource, currentTime))
                .flatMap(Collection::stream)
                .toList();
        log.info("Collected {} skole memberships", skoleMemberships.size());

        membershipEntityProducerService.publishChangedMemberships(skoleMemberships);

        List<Membership> undervisningsgruppeMemberships = undervisningsgruppeService.getAllValid(currentTime)
                .stream()
                .map(undervisningsgruppeResource -> eduMembershipService.createUndervisningsgruppeMembershipList(undervisningsgruppeResource, currentTime))
                .flatMap(Collection::stream)
                .toList();
        log.info("Collected {} undervisningsgruppe memberships", undervisningsgruppeMemberships.size());

        membershipEntityProducerService.publishChangedMemberships(undervisningsgruppeMemberships);
    }
}
