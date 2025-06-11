package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsertypeRoleServiceTest {

    @Mock
    private RoleService roleService;

    @Mock
    private OrganisasjonselementService organisasjonselementService;

    @InjectMocks
    private UsertypeRoleService usertypeRoleService;

    private static final String MAIN_ORG_ID = "123";
    private static final String MAIN_ORG_NAME = "Test Org";
    private static final OrganisasjonselementResource MAIN_ORG_RESOURCE = createOrganisasjonselementResource();

    @Test
    void shouldCreateOneRolePerRoleUserType() {
        when(organisasjonselementService.getMainOrganisasjonselement())
                .thenReturn(Optional.of(MAIN_ORG_RESOURCE));

        for (RoleUserType roleUserType : RoleUserType.values()) {
            String roleType = roleUserType.toString().toLowerCase();
            when(roleService.createRoleId(MAIN_ORG_RESOURCE, roleType, null, false))
                    .thenReturn("generated-role-id-" + roleType);
        }

        List<Role> roles = usertypeRoleService.createUserTypeRoles();

        assertEquals(RoleUserType.values().length, roles.size());

        for (Role role : roles) {
            assertTrue(role.getRoleId().startsWith("generated-role-id-"));
            assertFalse(role.getAggregatedRole());
            assertEquals(MAIN_ORG_ID, role.getOrganisationUnitId());
            assertEquals(MAIN_ORG_NAME, role.getOrganisationUnitName());
            assertEquals("http://resource/id", role.getResourceId());
        }
    }

    @Test
    void shouldThrowExceptionWhenMainOrgUnitNotFound() {
        when(organisasjonselementService.getMainOrganisasjonselement())
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> usertypeRoleService.createUserTypeRoles());

        assertEquals("Main organization unit not found", exception.getMessage());
    }

    @Test
    void shouldCreateSingleUserTypeRoleCorrectly() {
        OrganisasjonselementResource orgUnit = MAIN_ORG_RESOURCE;

        RoleUserType roleUserType = RoleUserType.STUDENT;


        when(roleService.createRoleId(orgUnit, "student", null, false))
                .thenReturn("generated-role-id-student");

        Role role = usertypeRoleService.createUserTypeRole(roleUserType, orgUnit);

        assertEquals("generated-role-id-student", role.getRoleId());
        assertEquals("student", role.getRoleType());
        assertEquals(MAIN_ORG_ID, role.getOrganisationUnitId());
        assertEquals(MAIN_ORG_NAME, role.getOrganisationUnitName());
        assertEquals("http://resource/id", role.getResourceId());
        assertFalse(role.getAggregatedRole());

    }

    private static OrganisasjonselementResource createOrganisasjonselementResource() {
        OrganisasjonselementResource orgElement = new OrganisasjonselementResource();
        Identifikator organisasjonsId = new Identifikator();
        organisasjonsId.setIdentifikatorverdi(UsertypeRoleServiceTest.MAIN_ORG_ID);
        orgElement.setOrganisasjonsId(organisasjonsId);
        orgElement.setNavn(UsertypeRoleServiceTest.MAIN_ORG_NAME);
        orgElement.addSelf(new Link("http://resource/id"));
        return orgElement;
    }
}