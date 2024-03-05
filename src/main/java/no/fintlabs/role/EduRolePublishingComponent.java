package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
public class EduRolePublishingComponent {
    private final BasisgruppeService basisgruppeService;
    private final RoleEntityProducerService roleEntityProducerService;
    private final SkoleService skoleService;
    private final EduRoleService eduRoleService;

    public EduRolePublishingComponent(
            SkoleService skoleService,
            BasisgruppeService basisgruppeService,
            RoleEntityProducerService roleEntityProducerService,
            EduRoleService eduRoleService
    ) {
        this.skoleService = skoleService;
        this.basisgruppeService = basisgruppeService;
        this.roleEntityProducerService = roleEntityProducerService;
        this.eduRoleService = eduRoleService;
    }
    @Scheduled(
            initialDelayString = "${fint.kontroll.role.edu-publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.edu-publishing.fixed-delay}"
    )
    public void publishEduRoles() {
        Date currentTime = Date.from(Instant.now());
        List<Role> validSkoleRoles = skoleService.getAll()
                .stream()
                .filter(skoleResource -> !skoleResource.getElevforhold().isEmpty())
                .map(skoleResource -> eduRoleService.createOptionalSkoleRole(skoleResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Role> publishedSkoleRoles = roleEntityProducerService.publishChangedRoles(validSkoleRoles);

        log.info("Published {} of {} valid skole roles", publishedSkoleRoles.size(), validSkoleRoles.size());
        log.debug("Ids of published basisgruppe roles: {}",
                publishedSkoleRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
      List<Role> validBasisgruppeRoles = basisgruppeService.getAllValid(currentTime)
                .stream()
                .filter(basisgruppeResource -> !basisgruppeResource.getElevforhold().isEmpty())
                .map(basisgruppeResource ->eduRoleService.createOptionalBasisgruppeRole(basisgruppeResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Role> publishedBasisgruppeRoles = roleEntityProducerService.publishChangedRoles(validBasisgruppeRoles);

        log.info("Published {} of {} valid basisgruppe roles", publishedBasisgruppeRoles.size(), validBasisgruppeRoles.size());
        log.debug("Ids of published basisgruppe roles: {}",
                publishedBasisgruppeRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }
}
