package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleServiceTest {
    private RoleService roleService;
    private List<String> eduOrgUnitIds;
    private OrganisasjonselementResource eduOrgUnit, nonEduOrgUnit;
    @Mock
    private FintCache<String, Role> roleCache;
    @Mock
    private OrganisasjonselementService organisasjonselementService;
    @Mock
    FintCache<String, RoleCatalogRole> roleCatalogRoleCache;

    @BeforeEach void setup() {
        roleService = new RoleService(roleCache, organisasjonselementService, roleCatalogRoleCache);
        eduOrgUnitIds = List.of("2");

        eduOrgUnit = new OrganisasjonselementResource();
        Identifikator organisasjonsId = new Identifikator();
        organisasjonsId.setIdentifikatorverdi("2");
        eduOrgUnit.setOrganisasjonsId(organisasjonsId);
        eduOrgUnit.setNavn("VGMIDT Midtbyen videregående skole");
        eduOrgUnit.addSelf(new Link("https://example.com/organisasjonsid/2"));

        nonEduOrgUnit = new OrganisasjonselementResource();
        Identifikator organisasjonsId2 = new Identifikator();
        organisasjonsId2.setIdentifikatorverdi("3");
        nonEduOrgUnit.setOrganisasjonsId(organisasjonsId2);
        nonEduOrgUnit.setNavn("VAR Vår fylkeskommune");
        nonEduOrgUnit.addSelf(new Link("https://example.com/organisasjonsid/3"));
    }

    @Test
    public void givenEduOrgUnit_createOptionalRole_shouldReturnRoleWithRoletypeEMPLOYEEFACTULTY() {

        Optional<Role> result = roleService.createOptionalOrgUnitRole(eduOrgUnit, eduOrgUnitIds, new Date());

        assertThat(result).isPresent();
        Role role = result.get();

        assertThat(role.getRoleType()).isEqualTo(RoleUserType.EMPLOYEEFACULTY.name());
    }
    @Test
    public void givenNonEduOrgUnit_createOptionalRole_shouldReturnRoleWithRoletypeEMPLOYEESTAFF() {

        Optional<Role> result = roleService.createOptionalOrgUnitRole(nonEduOrgUnit, eduOrgUnitIds, new Date());

        assertThat(result).isPresent();
        Role role = result.get();

        assertThat(role.getRoleType()).isEqualTo(RoleUserType.EMPLOYEESTAFF.name());
    }
}
