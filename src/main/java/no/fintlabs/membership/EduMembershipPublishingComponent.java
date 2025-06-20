package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.SkoleService;
import no.fintlabs.role.UndervisningsgruppeService;
import no.fintlabs.user.UserService;
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
    private final MembershipEntityProducerService membershipEntityProducerService;
    private final UserService userService;

    public EduMembershipPublishingComponent(SkoleService skoleService, UndervisningsgruppeService undervisningsgruppeService, EduMembershipService eduMembershipService, MembershipEntityProducerService membershipEntityProducerService, UserService userService) {
        this.skoleService = skoleService;
        this.undervisningsgruppeService = undervisningsgruppeService;
        this.eduMembershipService = eduMembershipService;
        this.membershipEntityProducerService = membershipEntityProducerService;
        this.userService = userService;
    }

    @Scheduled(cron = "${fint.kontroll.role.edu-publishing.cron}")
    public void publishEduRoleMembershipss() {
        Date currentTime = Date.from(Instant.now());

        Long noOfUsersInCache = userService.getNumberOfUsersInCache();
        log.info("Start collecting all edu memberships with {} users in the kontrolluser cache", noOfUsersInCache);

        List<Membership> skoleMemberships = skoleService.getAll()
                .stream()
                .map(skoleResource -> eduMembershipService.createSkoleMembershipList (skoleResource, currentTime))
                .flatMap(Collection::stream)
                .toList();
        log.info("Collected {} skole memberships", skoleMemberships.size());

        List<Membership> changedSkoleMemberships = membershipEntityProducerService.publishChangedMemberships(skoleMemberships);
        log.info("Published {} of {} skole memberships", changedSkoleMemberships.size(), skoleMemberships.size());

        List<Membership> undervisningsgruppeMemberships = undervisningsgruppeService.getAllValid(currentTime)
                .stream()
                .map(undervisningsgruppeResource -> eduMembershipService.createUndervisningsgruppeMembershipList(undervisningsgruppeResource, currentTime))
                .flatMap(Collection::stream)
                .toList();
        log.info("Collected {} undervisningsgruppe memberships", undervisningsgruppeMemberships.size());

        List<Membership> changedUndervisningsgruppeMemberships = membershipEntityProducerService.publishChangedMemberships(undervisningsgruppeMemberships);
        log.info("Published {} of {} undervisningsgruppe memberships", changedUndervisningsgruppeMemberships.size(), undervisningsgruppeMemberships.size());
    }
}
