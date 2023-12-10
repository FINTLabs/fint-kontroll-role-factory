package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
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

    public RolePublishingComponent(
            RoleEntityProducerService roleEntityProducerService,
            OrganisasjonselementService organisasjonselementService,
            RoleService roleService
    ) {
        this.roleEntityProducerService = roleEntityProducerService;
        this.organisasjonselementService = organisasjonselementService;
        this.roleService = roleService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.publishing.fixed-delay}"
    )
        public void publishRoles() {
        Date currentTime = Date.from(Instant.now());

        List<Role> validOrgUnitRoles = organisasjonselementService.getAllValid(currentTime)
                .stream()
                .map(organisasjonselementResource -> roleService.createOptionalOrgUnitRole(organisasjonselementResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(role -> {if (role.getMembers()==null ||role.getMembers().isEmpty()) {
                    log.info("Role {} has no members and will not be published", role.getRoleId());
                }
                })
                .filter(role -> role.getMembers()!=null && !role.getMembers().isEmpty())
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
