package no.fintlabs.role;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class EduRolePublishingComponent {
    private final UndervisningsgruppeService undervisningsgruppeService;
    private final RoleEntityProducerService roleEntityProducerService;
    private final SkoleService skoleService;
    private final EduRoleService eduRoleService;

    public EduRolePublishingComponent(
            SkoleService skoleService,
            UndervisningsgruppeService undervisningsgruppeService,
            RoleEntityProducerService roleEntityProducerService,
            EduRoleService eduRoleService
            ) {
        this.skoleService = skoleService;
        this.undervisningsgruppeService = undervisningsgruppeService;
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
        log.debug("Ids of published undervisningsgruppe roles: {}",
                publishedSkoleRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
      List<Role> validUndervisningsgruppeRoles = undervisningsgruppeService.getAllValid(currentTime)
                .stream()
                .filter(undervisningsgruppeResource -> !undervisningsgruppeResource.getElevforhold().isEmpty())
                .map(undervisningsgruppeResource ->eduRoleService.createOptionalUndervisningsgruppeRole(undervisningsgruppeResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Role> publishedUndervisningsgruppeRoles = roleEntityProducerService.publishChangedRoles(validUndervisningsgruppeRoles);

        log.info("Published {} of {} valid undervisningsgruppe roles", publishedUndervisningsgruppeRoles.size(), validUndervisningsgruppeRoles.size());
        log.debug("Ids of published undervisningsgruppe roles: {}",
                publishedUndervisningsgruppeRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }
}
