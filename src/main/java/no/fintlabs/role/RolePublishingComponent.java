package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.membership.AdmMembershipService;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class RolePublishingComponent {
    private final RoleEntityProducerService roleEntityProducerService;
    private final OrganisasjonselementService organisasjonselementService;
    private final RoleService roleService;
    private final EduOrgUnitService eduOrgUnitService;

    public RolePublishingComponent(
            RoleEntityProducerService roleEntityProducerService,
            OrganisasjonselementService organisasjonselementService,
            RoleService roleService, EduOrgUnitService eduOrgUnitService
    ) {
        this.roleEntityProducerService = roleEntityProducerService;
        this.organisasjonselementService = organisasjonselementService;
        this.roleService = roleService;
        this.eduOrgUnitService = eduOrgUnitService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.publishing.fixed-delay}"
    )
        public void publishRoles() {
        Date currentTime = Date.from(Instant.now());

        log.info("Searching for edu org units");
        List<String> eduOrgUnitIds = eduOrgUnitService.findAllEduOrgUnits();
        log.info("Found {} edu org units: {}", eduOrgUnitIds.size(), eduOrgUnitIds);

        List<Role> validOrgUnitRoles = organisasjonselementService.getAll()
                .stream()
                .map(organisasjonselementResource
                        -> roleService.createOptionalOrgUnitRole(
                            organisasjonselementResource,
                            eduOrgUnitIds,
                            currentTime
                        )
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List< Role > publishedRoles = roleEntityProducerService.publishChangedRoles(validOrgUnitRoles);

        log.info("Published {} of {} valid org unit roles", publishedRoles.size(), validOrgUnitRoles.size());
        log.info("Ids of published org unit roles: {}",
                publishedRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }
}
