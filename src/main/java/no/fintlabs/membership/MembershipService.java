package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.role.RoleCatalogRole;
import no.fintlabs.role.RoleService;
import no.fintlabs.user.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MembershipService {
    final RoleService roleService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;

    public MembershipService(RoleService roleService, FintCache<String, RoleCatalogRole> roleCatalogRoleCache) {
        this.roleService = roleService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
    }

    public Membership createMembership(
            RoleCatalogRole roleCatalogRole,
            User user,
            String membershipStatus,
            Date membershipStatusDate
    ){
        return Membership.builder()
                .roleId(roleCatalogRole.getId())
                .memberId(user.getId())
                .memberStatus(membershipStatus)
                .memberStatusChanged(membershipStatusDate)
                .build();
    }
}
