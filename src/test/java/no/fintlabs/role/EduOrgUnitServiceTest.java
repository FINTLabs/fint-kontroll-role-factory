package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class})
public class EduOrgUnitServiceTest {
    private EduOrgUnitService eduOrgUnitService;
    @Mock
    private  FintCache<String, SkoleResource> skoleResourceFintCache;
    @Mock
    private OrganisasjonselementService organisasjonselementService;

    @BeforeEach void setUp() {
        eduOrgUnitService = new EduOrgUnitService(skoleResourceFintCache, organisasjonselementService);
    }
    @Test
    public void givenSkoleResourceCacheIsEmpty_findAllEduOrgUnits_should_return_null() {

        given(skoleResourceFintCache.getAll()).willReturn(null);

        List<String> result = eduOrgUnitService.findAllEduOrgUnits();

        assertNull(result);
    }
    @Test
    public void givenSkoleResourceCacheIsNonEmpty_findAllEduOrgUnits_should_return_eduOrgUnitids() {
        OrganisasjonselementResource orgunitSchool = new OrganisasjonselementResource();
        OrganisasjonselementResource orgunitSchoolSubUnit1 = new OrganisasjonselementResource();
        OrganisasjonselementResource orgunitSchoolSubUnit2 = new OrganisasjonselementResource();
        SkoleResource skoleResource = new SkoleResource();

        Link link = new Link("https://example.com/organisasjonsid/2");
        orgunitSchool.addSelf(link);
        skoleResource.addOrganisasjon(link);

        Identifikator id2 = new Identifikator();
        id2.setIdentifikatorverdi("2");
        orgunitSchool.setOrganisasjonsId(id2);
        orgunitSchool.setNavn("School");

        Identifikator id3 = new Identifikator();
        id3.setIdentifikatorverdi("3");
        orgunitSchoolSubUnit1.setOrganisasjonsId(id3);
        orgunitSchoolSubUnit1.setNavn("SchoolSubUnit1");

        Identifikator id4 = new Identifikator();
        id4.setIdentifikatorverdi("4");
        orgunitSchoolSubUnit2.setOrganisasjonsId(id4);
        orgunitSchoolSubUnit2.setNavn("SchoolSubUnit2");

        given(organisasjonselementService.getOrganisasjonselementResource(link.getHref())).willReturn(Optional.of(orgunitSchool));
        given(organisasjonselementService.getAllSubOrgUnits(orgunitSchool)).willReturn(List.of(orgunitSchool, orgunitSchoolSubUnit1, orgunitSchoolSubUnit2));
        given(skoleResourceFintCache.getAll()).willReturn(List.of(skoleResource));

        List<String> result = eduOrgUnitService.findAllEduOrgUnits();
        List<String> expectedOrgUnitIds = List.of("2","3", "4");

        assertEquals(expectedOrgUnitIds, result);
    }

}
