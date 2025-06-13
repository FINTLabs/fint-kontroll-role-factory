package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.membership.Membership;
import no.fintlabs.membership.MembershipEntityProducerService;
import no.fintlabs.membership.UserTypeMembershipService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserTypeRolePublishingComponent {

    private final UsertypeRoleService usertypeRoleService;
    private final RoleEntityProducerService roleEntityProducerService;
    private final UserTypeMembershipService userTypeMembershipService;
    private final MembershipEntityProducerService membershipEntityProducerService;



    public UserTypeRolePublishingComponent(UsertypeRoleService usertypeRoleService, RoleEntityProducerService roleEntityProducerService, UserTypeMembershipService userTypeMembershipService, MembershipEntityProducerService membershipEntityProducerService) {
        this.usertypeRoleService = usertypeRoleService;
        this.roleEntityProducerService = roleEntityProducerService;
        this.userTypeMembershipService = userTypeMembershipService;
        this.membershipEntityProducerService = membershipEntityProducerService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.usertype-role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.usertype-role.publishing.fixed-delay}"
    )
    public void publishUserTypeRolesAndMemberships() {
        List<Role> userTypeRoles = usertypeRoleService.createUserTypeRoles();

        publishUserTypeRoles(userTypeRoles);

        for (Role userTypeRole : userTypeRoles) {
            publishMembershipsForUserTypeRole(userTypeRole);
        }
    }

    public void publishUserTypeRoles(List<Role> userTypeRoles) {
        List<Role> publishedRoles = roleEntityProducerService.publishChangedRoles(userTypeRoles);

        log.info("Published {} of {} valid  roles", publishedRoles.size(), userTypeRoles.size());
        log.info("Ids of published user type roles: {}",
                publishedRoles.stream()
                        .map(Role::getRoleId)
                        .toList()
        );
    }

    public void publishMembershipsForUserTypeRole(Role userTypeRole ) {
        log.info("Finding memberships for user type role: {}", userTypeRole.getRoleName());
        List<Membership> memberships = userTypeMembershipService.createUserTypeMembershipList(userTypeRole);

        if (memberships.isEmpty()) {
            log.info("No memberships found for user type role: {}", userTypeRole.getRoleName());
            return;
        }
        List <Membership> publishedMemberships = membershipEntityProducerService.publishChangedMemberships(memberships);
        log.info("Published {} of {} memberships for user type role: {}",
                publishedMemberships.size(), memberships.size(), userTypeRole.getRoleName());
    }
}
