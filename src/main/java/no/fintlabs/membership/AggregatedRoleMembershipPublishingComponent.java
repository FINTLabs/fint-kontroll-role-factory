package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.role.RoleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;


@Slf4j
@Component
public class AggregatedRoleMembershipPublishingComponent {
    private final OrganisasjonselementService organisasjonselementService;
    private final AdmMembershipService admMembershipService;
    private final RoleService roleService;
    private final MembershipEntityProducerService membershipEntityProducerService;

    public AggregatedRoleMembershipPublishingComponent(OrganisasjonselementService organisasjonselementService, AdmMembershipService admMembershipService, RoleService roleService, MembershipEntityProducerService membershipEntityProducerService) {
        this.organisasjonselementService = organisasjonselementService;
        this.admMembershipService = admMembershipService;
        this.roleService = roleService;
        this.membershipEntityProducerService = membershipEntityProducerService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.aggregated-role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.aggregated-role.publishing.fixed-delay}"
    )
    public void publishAggregatedRoles() {

        List<Membership> allMemberships = roleService.getAllAggregatedOrgUnitRoles()
                .stream()
                .filter(Objects::nonNull)
                .map(admMembershipService::createAggregatedOrgUnitMembershipList)
                .flatMap(Collection::stream)
                .toList();

        log.info("Collecting {} aggregated org unit memberships", allMemberships.size());

        List<Membership> publishedAggrMemberships = membershipEntityProducerService.publishChangedMemberships(allMemberships);

        log.info("Published {} of {} valid aggregated org unit memberships", publishedAggrMemberships.size(), allMemberships.size());
    }
}


