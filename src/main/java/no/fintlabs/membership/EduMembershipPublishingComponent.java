package no.fintlabs.membership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fintlabs.role.SkoleService;
import no.fintlabs.role.UndervisningsgruppeService;
import no.fintlabs.user.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;

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
    private record MembershipKey(Long roleId, Long memberId) {}


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

        int publishedSkoleMembershipCount = membershipEntityProducerService.publishChangedMemberships(skoleMemberships);
        log.info("Published {} of {} skole memberships", publishedSkoleMembershipCount, skoleMemberships.size());

        List<Membership> undervisningsgruppeMemberships = undervisningsgruppeService.getAllValid()
                .stream()
                .map(undervisningsgruppeResource -> createUndervisningsgruppeMemberships(undervisningsgruppeResource, currentTime))
                .flatMap(Collection::stream)
                .toList();
        log.debug("Collected {} undervisningsgruppe memberships before deduplication", undervisningsgruppeMemberships.size());

        undervisningsgruppeMemberships = deduplicateByRoleAndMemberIdFavoringActive(undervisningsgruppeMemberships);

        log.info("Collected {} undervisningsgruppe memberships after deduplication", undervisningsgruppeMemberships.size());

        int publishedUndervisningsgruppeMembershipCount = membershipEntityProducerService.publishChangedMemberships(undervisningsgruppeMemberships);
        log.info("Published {} of {} undervisningsgruppe memberships", publishedUndervisningsgruppeMembershipCount, undervisningsgruppeMemberships.size());
    }

    private List<Membership> createUndervisningsgruppeMemberships(
            UndervisningsgruppeResource undervisningsgruppeResource,
            Date currentTime
    ) {
        String roleStatus = getUndervisningsgruppeRoleStatus(undervisningsgruppeResource, currentTime);

        log.debug("Undervisningsgruppe {} has status {}", undervisningsgruppeResource.getSystemId(), roleStatus);

        return eduMembershipService.createUndervisningsgruppeMembershipList(
                undervisningsgruppeResource,
                currentTime,
                roleStatus
        );
    }

    private List<Membership> deduplicateByRoleAndMemberIdFavoringActive(List<Membership> memberships) {
        Map<MembershipKey, Membership> membershipsByRoleAndMemberId = new LinkedHashMap<>();

        memberships.forEach(membership -> membershipsByRoleAndMemberId.merge(
                new MembershipKey(membership.getRoleId(), membership.getMemberId()),
                membership,
                (membership1, membership2) -> {
                    log.debug("Duplicate role id {} and member id {}. Favoring active membership", membership.getRoleId(), membership.getMemberId());
                    return isActive(membership2) && !isActive(membership1)
                            ? membership2
                            : membership1;
                }
        ));

        return List.copyOf(membershipsByRoleAndMemberId.values());
    }

    private boolean isActive(Membership membership) {
        return "ACTIVE".equals(membership.getMemberStatus());
    }
}
