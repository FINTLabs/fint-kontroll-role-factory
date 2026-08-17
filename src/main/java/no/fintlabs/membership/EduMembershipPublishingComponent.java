package no.fintlabs.membership;

import lombok.RequiredArgsConstructor;
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

import static no.fintlabs.utils.RoleUtils.getUndervisningsgruppeRoleStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class EduMembershipPublishingComponent {
    private final SkoleService skoleService;
    private final UndervisningsgruppeService undervisningsgruppeService;
    private final EduMembershipService eduMembershipService;
    private final MembershipEntityProducerService membershipEntityProducerService;
    private final UserService userService;
    public record Pair<K, V>(K key, V value) {}


    @Scheduled(cron = "${fint.kontroll.role.edu-publishing.cron}")
    public void publishEduRoleMembershipss() {
        Date currentTime = Date.from(Instant.now());

        Long noOfUsersInCache = userService.getNumberOfUsersInCache();
        log.info("Start collecting all edu memberships with {} users in the kontrolluser cache", noOfUsersInCache);

        List<Membership> skoleMemberships = skoleService.getAll()
                .stream()
                .map(skoleResource -> eduMembershipService.createSkoleMembershipList(skoleResource, currentTime))
                .flatMap(Collection::stream)
                .toList();
        log.info("Collected {} skole memberships", skoleMemberships.size());

        List<Membership> changedSkoleMemberships = membershipEntityProducerService.publishChangedMemberships(skoleMemberships);
        log.info("Published {} of {} skole memberships", changedSkoleMemberships.size(), skoleMemberships.size());

        List<Membership> undervisningsgruppeMemberships = undervisningsgruppeService.getAllValid()
                .stream()
                .map(undervisningsgruppeResource -> new Pair<>(undervisningsgruppeResource, getUndervisningsgruppeRoleStatus(undervisningsgruppeResource, currentTime)))
                .peek(pair -> log.info("Undervisningsgruppe {} has status {}", pair.key, pair.value))
                .map(undervisningsgruppeResource -> eduMembershipService.createUndervisningsgruppeMembershipList(undervisningsgruppeResource.key, currentTime, undervisningsgruppeResource.value))
                .flatMap(Collection::stream)
                .toList();
        log.info("Collected {} undervisningsgruppe memberships", undervisningsgruppeMemberships.size());

        List<Membership> changedUndervisningsgruppeMemberships = membershipEntityProducerService.publishChangedMemberships(undervisningsgruppeMemberships);
        log.info("Published {} of {} undervisningsgruppe memberships", changedUndervisningsgruppeMemberships.size(), undervisningsgruppeMemberships.size());
    }
}
