package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EduRoleServiceTest {

    @Mock
    private OrganisasjonselementService organisasjonselementService;

    @Mock
    private SkoleService skoleService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private EduRoleService eduRoleService;

    @Test
    void givenSkoleOrgUnitWithoutGyldighetsperiode_createOptionalSkoleRole_shouldReturnRoleWithoutDates() {
        SkoleResource skoleResource = createSkoleResource();
        OrganisasjonselementResource orgUnit = createOrganisasjonselementResource();

        when(organisasjonselementService.getOrganisasjonsResource(skoleResource)).thenReturn(Optional.of(orgUnit));
        when(roleService.createSkoleRoleId(skoleResource, RoleType.ELEV.getRoleType())).thenReturn("elev@123");
        when(roleService.createSchoolRoleName(
                skoleResource.getNavn(),
                "VGMIDT",
                RoleType.ELEV.getRoleType(),
                RoleSubType.SKOLEGRUPPE.getRoleSubType())
        ).thenReturn("Elev - VGMIDT Alle elever Midtbyen videregående skole");

        Optional<Role> result = eduRoleService.createOptionalSkoleRole(skoleResource);

        assertThat(result).isPresent();
        assertThat(result.get().getRoleStatus()).isEqualTo("ACTIVE");
        assertThat(result.get().getStartDate()).isNull();
        assertThat(result.get().getEndDate()).isNull();
    }

    private SkoleResource createSkoleResource() {
        SkoleResource skoleResource = new SkoleResource();
        skoleResource.setNavn("Midtbyen videregående skole");
        skoleResource.setSkolenummer(createIdentifikator("123"));
        skoleResource.addSelf(new Link("https://example.com/skole/123"));
        return skoleResource;
    }

    private OrganisasjonselementResource createOrganisasjonselementResource() {
        OrganisasjonselementResource orgUnit = new OrganisasjonselementResource();
        orgUnit.setOrganisasjonsId(createIdentifikator("123"));
        orgUnit.setNavn("VGMIDT Midtbyen videregående skole");
        orgUnit.addSelf(new Link("https://example.com/organisasjonsid/123"));
        return orgUnit;
    }

    private Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }
}
