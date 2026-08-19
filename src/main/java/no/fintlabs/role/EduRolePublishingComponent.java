package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

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

    @Scheduled(cron = "${fint.kontroll.role.edu-publishing.cron}")
    public void publishEduRoles() {
        Date currentTime = Date.from(Instant.now());
        List<Role> validSkoleRoles = skoleService.getAll()
                .stream()
                .filter(skoleResource -> skoleResource.getElevforhold() != null && !skoleResource.getElevforhold().isEmpty())
                .map(eduRoleService::createOptionalSkoleRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Role> publishedSkoleRoles = roleEntityProducerService.publishChangedRoles(validSkoleRoles);

        log.info("Published {} of {} valid skole roles", publishedSkoleRoles.size(), validSkoleRoles.size());
        log.debug("Ids of published skole roles: {}",
                publishedSkoleRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
        List<Role> validUndervisningsgruppeRoles = undervisningsgruppeService.getAllValid()
                .stream()
                .filter(undervisningsgruppeResource -> !undervisningsgruppeResource.getElevforhold().isEmpty())
                .map(undervisningsgruppeResource -> eduRoleService.createOptionalUndervisningsgruppeRole(undervisningsgruppeResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        validUndervisningsgruppeRoles = deduplicateByRoleIdFavoringActive(validUndervisningsgruppeRoles);

        List<Role> publishedUndervisningsgruppeRoles = roleEntityProducerService.publishChangedRoles(validUndervisningsgruppeRoles);

        log.info("Published {} of {} valid undervisningsgruppe roles", publishedUndervisningsgruppeRoles.size(), validUndervisningsgruppeRoles.size());
        log.debug("Ids of published undervisningsgruppe roles: {}",
                publishedUndervisningsgruppeRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }

    private List<Role> deduplicateByRoleIdFavoringActive(List<Role> roles) {
        Map<String, Role> rolesByRoleId = new LinkedHashMap<>();

        roles.forEach(role -> rolesByRoleId.merge(
                role.getRoleId(),
                role,
                (role1, role2) -> {
                    log.debug("Duplicate role id {}. Favoring active role", role.getRoleId());
                    return isActive(role2) && !isActive(role1) ? role2 : role1;
                }
        ));

        return List.copyOf(rolesByRoleId.values());
    }

    private boolean isActive(Role role) {
        return "ACTIVE".equals(role.getRoleStatus());
    }
}
