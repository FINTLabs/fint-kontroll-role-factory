package no.fintlabs.organisasjonselement;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.cache.FintCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisasjonselementServiceTest {

    @Mock
    private FintCache<String, OrganisasjonselementResource> organisasjonselementCache;


    @InjectMocks
    private OrganisasjonselementService organisasjonselementService;


    @Test
    void shouldReturnMainOrganisasjonselementResource() {
        String selfLink = "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/123-A";
        String overordnetLink = "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/123-A";
        Identifikator id = new Identifikator();
        id.setIdentifikatorverdi("123");
        Identifikator kode = new Identifikator();
        kode.setIdentifikatorverdi("MAIN");
        OrganisasjonselementResource mainOrg = new OrganisasjonselementResource();
        mainOrg.setOrganisasjonsId(id);
        mainOrg.setOrganisasjonsKode(kode);
        mainOrg.addSelf(new Link(selfLink));
        mainOrg.addOverordnet(new Link(overordnetLink));

        when(organisasjonselementCache.getAllDistinct()).thenReturn(List.of(mainOrg));

        Optional<OrganisasjonselementResource> result = organisasjonselementService.getMainOrganisasjonselement();

        assertThat(result).isPresent();
        assertThat(result.get().getOrganisasjonsKode().getIdentifikatorverdi()).isEqualTo("MAIN");
        assertThat(result.get().getOrganisasjonsId().getIdentifikatorverdi()).isEqualTo("123");
    }

    @Test
    void shouldCorrectlyLowerCaseWhenLookingForMainOrganisasjonselement() {
        String selfLink = "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/123-A";
        String overordnetLink = "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsId/123-A";
        Identifikator id = new Identifikator();
        id.setIdentifikatorverdi("123");
        Identifikator kode = new Identifikator();
        kode.setIdentifikatorverdi("MAIN");
        OrganisasjonselementResource mainOrg = new OrganisasjonselementResource();
        mainOrg.setOrganisasjonsId(id);
        mainOrg.setOrganisasjonsKode(kode);
        mainOrg.addSelf(new Link(selfLink));
        mainOrg.addOverordnet(new Link(overordnetLink));

        when(organisasjonselementCache.getAllDistinct()).thenReturn(List.of(mainOrg));

        Optional<OrganisasjonselementResource> result = organisasjonselementService.getMainOrganisasjonselement();

        assertThat(result).isPresent();
        assertThat(result.get().getOrganisasjonsKode().getIdentifikatorverdi()).isEqualTo("MAIN");
        assertThat(result.get().getOrganisasjonsId().getIdentifikatorverdi()).isEqualTo("123");
    }

    @Test
    void shouldBeCaseSensitiveForOrganisasjonskodeWhenLookingForMainOrganisasjonselement() {
        String selfLink = "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonskode/123-A";
        String overordnetLink = "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonskode/123-a";
        Identifikator id = new Identifikator();
        id.setIdentifikatorverdi("123");
        Identifikator kode = new Identifikator();
        kode.setIdentifikatorverdi("MAIN");
        OrganisasjonselementResource mainOrg = new OrganisasjonselementResource();
        mainOrg.setOrganisasjonsId(id);
        mainOrg.setOrganisasjonsKode(kode);
        mainOrg.addSelf(new Link(selfLink));
        mainOrg.addOverordnet(new Link(overordnetLink));

        when(organisasjonselementCache.getAllDistinct()).thenReturn(List.of(mainOrg));

        Optional<OrganisasjonselementResource> result = organisasjonselementService.getMainOrganisasjonselement();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoMainOrganisasjonselementResource() {
        when(organisasjonselementCache.getAllDistinct()).thenReturn(List.of());

        Optional<OrganisasjonselementResource> result = organisasjonselementService.getMainOrganisasjonselement();

        assertThat(result).isEmpty();
    }
    @Test
    void shouldNotFailIfOverordnetLinkIsMissingWhenLookingForMainOrganisasjonselement() {
        String selfLink = "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonskode/123-A";
        Identifikator id = new Identifikator();
        id.setIdentifikatorverdi("123");
        Identifikator kode = new Identifikator();
        kode.setIdentifikatorverdi("MAIN");
        OrganisasjonselementResource mainOrg = new OrganisasjonselementResource();
        mainOrg.setOrganisasjonsId(id);
        mainOrg.setOrganisasjonsKode(kode);
        mainOrg.addSelf(new Link(selfLink));
        when(organisasjonselementCache.getAllDistinct()).thenReturn(List.of(mainOrg));

        Optional<OrganisasjonselementResource> result = organisasjonselementService.getMainOrganisasjonselement();

        assertThat(result).isEmpty();
    }

}