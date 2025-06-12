package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleCatalogRole;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserTypeMembershipService {
    private final UserService userService;
    private final MembershipService membershipService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;

    public UserTypeMembershipService(UserService userService, MembershipService membershipService, FintCache<String, RoleCatalogRole> roleCatalogRoleCache) {
        this.userService = userService;
        this.membershipService = membershipService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
    }

    public List<Membership> createUserTypeMembershipList(Role userTypeRole) {

        if (userTypeRole == null || userTypeRole.getRoleType() == null) {
            log.warn("User type role or role type is null, cannot create memberships.");
            return List.of();
        }

        log.info("Creating user type memberships for role: {} {}", userTypeRole.getRoleId(), userTypeRole.getRoleName());

        Optional<RoleCatalogRole> roleCatalogRole= roleCatalogRoleCache.getOptional(userTypeRole.getRoleId());

        if (roleCatalogRole.isEmpty()) {
            log.warn("RoleCatalogRole not found for role {}", userTypeRole.getRoleId());
            return List.of();
        }

        List<User> usersWithUserType = userService.getUsersWithUserType(userTypeRole.getRoleType());

        if (usersWithUserType.isEmpty()) {
            log.info("No users found for user type role: {}", userTypeRole.getRoleName());
            return List.of();
        }

        return usersWithUserType
                .stream()
                .map(user ->
                        membershipService.createMembership(
                                roleCatalogRole.get(),
                                user,
                                user.getStatus(),
                                user.getStatusChanged()))
                .toList();
    }
}
