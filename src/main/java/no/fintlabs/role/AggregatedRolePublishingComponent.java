package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.member.MemberService;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class AggregatedRolePublishingComponent {
    private final RoleEntityProducerService roleEntityProducerService;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;
    private  final SimpleMemberService simpleMemberService;
    private final MemberService memberService;
    private final RoleService roleService;

    public AggregatedRolePublishingComponent(RoleEntityProducerService roleEntityProducerService, OrganisasjonselementService organisasjonselementService, ArbeidsforholdService arbeidsforholdService, SimpleMemberService simpleMemberService, MemberService memberService, RoleService roleService) {
        this.roleEntityProducerService = roleEntityProducerService;
        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
        this.simpleMemberService = simpleMemberService;
        this.memberService = memberService;
        this.roleService = roleService;
    }
    @Scheduled(
            //initialDelay = 20000L,
            //fixedDelay = 20000L
            initialDelayString = "${fint.kontroll.aggregated-role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.aggregated-role.publishing.fixed-delay}"
    )
    public void publishAggregatedRoles() {
        Date currentTime = Date.from(Instant.now());

        List<Role> validAggrOrgUnitRoles = roleService.getAllNonAggregatedOrgUnitRoles()
                .stream()
                .filter(role -> role.getChildrenRoleIds() != null && !role.getChildrenRoleIds().isEmpty())
                .map(role ->roleService.createOptionalAggrOrgUnitRole(role))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(role -> {if (role.getMembers()==null ||role.getMembers().isEmpty()) {
                    log.info("Role {} has no members and will not be published", role.getRoleId());
                }
                })
                .filter(role -> role.getMembers()!=null && !role.getMembers().isEmpty())
                .toList();

        List< Role > publishedAggrRoles = roleEntityProducerService.publishChangedRoles(validAggrOrgUnitRoles);

        log.info("Published {} of {} valid aggregated org unit roles", publishedAggrRoles.size(), validAggrOrgUnitRoles.size());
        log.info("Ids of published aggregated org unit roles: {}",
                publishedAggrRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }
}
