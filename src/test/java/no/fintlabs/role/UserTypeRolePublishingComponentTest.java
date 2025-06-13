package no.fintlabs.role;

import no.fintlabs.membership.Membership;
import no.fintlabs.membership.MembershipEntityProducerService;
import no.fintlabs.membership.UserTypeMembershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserTypeRolePublishingComponentTest {

    @Mock
    private UsertypeRoleService usertypeRoleService;
    @Mock
    private RoleEntityProducerService roleEntityProducerService;
    @Mock
    private UserTypeMembershipService userTypeMembershipService;
    @Mock
    private MembershipEntityProducerService membershipEntityProducerService;
    @InjectMocks
    private UserTypeRolePublishingComponent component;

    private static final String ALL_TEACHERS = "all teachers";
    private static final String ALL_STUDENTS = "all students";

    @Test
    void shouldPublishUserTypeRoles() {
        Role role1 = Role.builder().roleId("role1").build();
        Role role2 = Role.builder().roleId("role2").build();
        List<Role> userTypeRoles = List.of(role1, role2);

        when(usertypeRoleService.createUserTypeRoles()).thenReturn(userTypeRoles);
        when(roleEntityProducerService.publishChangedRoles(userTypeRoles)).thenReturn(List.of(role1));

        component.publishUserTypeRolesAndMemberships();

        verify(usertypeRoleService).createUserTypeRoles();
        verify(roleEntityProducerService).publishChangedRoles(userTypeRoles);
    }

    @Test
    void shouldNotPublishMembershipsWhenNoneFound() {
        Role role = Role.builder().roleId("role1").roleName(ALL_TEACHERS).build();

        when(userTypeMembershipService.createUserTypeMembershipList(role)).thenReturn(List.of());

        component.publishMembershipsForUserTypeRole(role);

        verify(membershipEntityProducerService, never()).publishChangedMemberships(any());
    }

    @Test
    void shouldPublishMembershipsWhenFound() {
        Role role = Role.builder().roleId("role1").roleName(ALL_STUDENTS).build();
        Membership m1 = Membership.builder().build();
        Membership m2 = Membership.builder().build();

        when(userTypeMembershipService.createUserTypeMembershipList(role)).thenReturn(List.of(m1, m2));
        when(membershipEntityProducerService.publishChangedMemberships(List.of(m1, m2)))
                .thenReturn(List.of(m1));

        component.publishMembershipsForUserTypeRole(role);

        verify(membershipEntityProducerService).publishChangedMemberships(List.of(m1, m2));
    }

    @Test
    void shouldPublishRolesAndThenMemberships() {
        Role r1 = Role.builder().roleId("r1").roleName(ALL_TEACHERS).build();
        Role r2 = Role.builder().roleId("r2").roleName(ALL_STUDENTS).build();
        Membership m1 = Membership.builder().build();

        when(usertypeRoleService.createUserTypeRoles()).thenReturn(List.of(r1, r2));
        when(roleEntityProducerService.publishChangedRoles(any())).thenReturn(List.of(r1, r2));
        when(userTypeMembershipService.createUserTypeMembershipList(any()))
                .thenReturn(List.of(m1));

        when(membershipEntityProducerService.publishChangedMemberships(any()))
                .thenReturn(List.of(m1));

        component.publishUserTypeRolesAndMemberships();

        verify(usertypeRoleService).createUserTypeRoles();
        verify(roleEntityProducerService).publishChangedRoles(any());
        verify(userTypeMembershipService, times(2)).createUserTypeMembershipList(any());
        verify(membershipEntityProducerService, times(2)).publishChangedMemberships(any());
    }
}
