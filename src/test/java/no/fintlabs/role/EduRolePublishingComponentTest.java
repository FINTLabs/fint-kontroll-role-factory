package no.fintlabs.role;

import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EduRolePublishingComponentTest {

    @Mock
    private UndervisningsgruppeService undervisningsgruppeService;

    @Mock
    private RoleEntityProducerService roleEntityProducerService;

    @Mock
    private SkoleService skoleService;

    @Mock
    private EduRoleService eduRoleService;

    @InjectMocks
    private EduRolePublishingComponent component;

    @Test
    void publishEduRolesDeduplicatesUndervisningsgruppeRolesAndFavorsActiveStatus() {
        UndervisningsgruppeResource inactiveGroup = createUndervisningsgruppeResource("inactive");
        UndervisningsgruppeResource activeGroup = createUndervisningsgruppeResource("active");
        UndervisningsgruppeResource otherGroup = createUndervisningsgruppeResource("other");

        Role inactiveDuplicateRole = Role.builder().roleId("role-1").roleStatus("INACTIVE").build();
        Role activeDuplicateRole = Role.builder().roleId("role-1").roleStatus("ACTIVE").build();
        Role otherRole = Role.builder().roleId("role-2").roleStatus("INACTIVE").build();

        when(skoleService.getAll()).thenReturn(List.of());
        when(undervisningsgruppeService.getAllValid()).thenReturn(List.of(inactiveGroup, activeGroup, otherGroup));
        when(eduRoleService.createOptionalUndervisningsgruppeRole(eq(inactiveGroup), any(Date.class)))
                .thenReturn(Optional.of(inactiveDuplicateRole));
        when(eduRoleService.createOptionalUndervisningsgruppeRole(eq(activeGroup), any(Date.class)))
                .thenReturn(Optional.of(activeDuplicateRole));
        when(eduRoleService.createOptionalUndervisningsgruppeRole(eq(otherGroup), any(Date.class)))
                .thenReturn(Optional.of(otherRole));
        when(roleEntityProducerService.publishChangedRoles(any())).thenReturn(List.of());

        component.publishEduRoles();

        ArgumentCaptor<List<Role>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleEntityProducerService, times(2)).publishChangedRoles(rolesCaptor.capture());

        assertThat(rolesCaptor.getAllValues().get(0)).isEmpty();
        assertThat(rolesCaptor.getAllValues().get(1))
                .containsExactly(activeDuplicateRole, otherRole);
    }

    private UndervisningsgruppeResource createUndervisningsgruppeResource(String id) {
        UndervisningsgruppeResource resource = new UndervisningsgruppeResource();
        resource.addSelf(Link.with(id));
        resource.addElevforhold(Link.with("elevforhold-" + id));
        return resource;
    }
}
