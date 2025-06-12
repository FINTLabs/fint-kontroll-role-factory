package no.fintlabs.membership;

import no.fintlabs.cache.FintCache;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleCatalogRole;
import no.fintlabs.role.RoleType;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserTypeMembershipServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private MembershipService membershipService;
    @Mock
    private FintCache<String, RoleCatalogRole> roleCatalogRoleCache;
    @InjectMocks
    private UserTypeMembershipService userTypeMembershipService;

    private static final String ROLE_ID = "123";
    private static final String ROLE_NAME = "Test Role";

    @Test
    void shouldReturnEmptyListWhenRoleIsNull() {
        List<Membership> result = userTypeMembershipService.createUserTypeMembershipList(null);
        assertThat(result).isEmpty();
        verifyNoInteractions(roleCatalogRoleCache, userService, membershipService);
    }

    @Test
    void shouldReturnEmptyListWhenRoleTypeIsNull() {
        Role role = Role.builder().roleId(ROLE_ID).roleName(ROLE_NAME).build();
        // No roleType set

        List<Membership> result = userTypeMembershipService.createUserTypeMembershipList(role);
        assertThat(result).isEmpty();
        verifyNoInteractions(roleCatalogRoleCache, userService, membershipService);
    }

    @Test
    void shouldReturnEmptyListWhenRoleCatalogRoleNotFound() {
        Role role = Role.builder().roleId(ROLE_ID).roleName(ROLE_NAME).roleType(RoleType.LARER.name()).build();

        when(roleCatalogRoleCache.getOptional(ROLE_ID)).thenReturn(Optional.empty());

        List<Membership> result = userTypeMembershipService.createUserTypeMembershipList(role);
        assertThat(result).isEmpty();

        verify(roleCatalogRoleCache).getOptional(ROLE_ID);
        verifyNoInteractions(userService, membershipService);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersFound() {
        Role role = Role.builder().roleId(ROLE_ID).roleName(ROLE_NAME).roleType(RoleType.ELEV.name()).build();


        RoleCatalogRole catalogRole = new RoleCatalogRole();
        when(roleCatalogRoleCache.getOptional(ROLE_ID)).thenReturn(Optional.of(catalogRole));
        when(userService.getUsersWithUserType(RoleType.ELEV.name())).thenReturn(List.of());

        List<Membership> result = userTypeMembershipService.createUserTypeMembershipList(role);
        assertThat(result).isEmpty();

        verify(userService).getUsersWithUserType(RoleType.ELEV.name());
        verifyNoInteractions(membershipService);
    }

    @Test
    void shouldCreateMembershipsForValidUsers() {
        Role role = Role.builder().roleId(ROLE_ID).roleName(ROLE_NAME).roleType(RoleType.ANSATT.name()).build();

        RoleCatalogRole catalogRole = new RoleCatalogRole();

        User user1 = User.builder().id(1L).status("ACTIVE").statusChanged(Date.from(Instant.now())).build();
        User user2 = User.builder().id(2L).status("INACTIVE").statusChanged(Date.from(Instant.now())).build();

        Membership membership1 = Membership.builder().build();
        Membership membership2 = Membership.builder().build();

        when(roleCatalogRoleCache.getOptional(ROLE_ID)).thenReturn(Optional.of(catalogRole));
        when(userService.getUsersWithUserType(RoleType.ANSATT.name())).thenReturn(List.of(user1, user2));
        when(membershipService.createMembership(eq(catalogRole), eq(user1), eq("ACTIVE"), any()))
                .thenReturn(membership1);
        when(membershipService.createMembership(eq(catalogRole), eq(user2), eq("INACTIVE"), any()))
                .thenReturn(membership2);

        List<Membership> result = userTypeMembershipService.createUserTypeMembershipList(role);

        assertThat(result).containsExactly(membership1, membership2);
        verify(membershipService, times(2)).createMembership(any(), any(), any(), any());
    }
}
