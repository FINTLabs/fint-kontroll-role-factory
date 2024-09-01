package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleCatalogRole;
import no.fintlabs.role.RoleService;
import no.fintlabs.user.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MembershipService {
    final RoleService roleService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;
    private final FintCache<String, Membership> membershipCache;

    public MembershipService(
            RoleService roleService,
            FintCache<String, RoleCatalogRole> roleCatalogRoleCache,
            FintCache<String, Membership> membershipCache) {
        this.roleService = roleService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
        this.membershipCache = membershipCache;
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
    public List<Membership> createMembershipList(Role role){
        Optional<RoleCatalogRole> roleCatalogRole= roleCatalogRoleCache.getOptional(role.getRoleId());

        if (roleCatalogRole.isEmpty()) {
            log.warn("RoleCatalogRole not found for role {}", role.getRoleId());
            return List.of();
        }
        Optional<List<Membership>> memberships = getMembershipList(roleCatalogRole.get().getId());

        return memberships.orElseGet(List::of);
    }

    private Optional<List<Membership>> getMembershipList(Long roleId) {
       List<Membership> memberships= membershipCache.getAll()
                .stream()
                .filter(membership -> membership.getRoleId().equals(roleId))
                .toList();

       if (memberships.isEmpty()) {
           log.warn("No memberships found for role {}", roleId);
           return Optional.empty();
       }
       return Optional.of(memberships);
    }
}
