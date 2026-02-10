package no.fintlabs.termin;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fintlabs.base.BaseTest;
import no.fintlabs.cache.FintCache;
import no.fintlabs.role.GyldighetsperiodeService;
import no.fintlabs.role.RoleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminServiceTest extends BaseTest {

    @Mock
    private GyldighetsperiodeService gyldighetsperiodeService;

    @Mock
    private FintCache<String, TerminResource> terminResourceCache;

    private TerminService terminService;
    private UndervisningsgruppeResource undervisningsgruppeResource;
    private Link validTerminLink, invalidTerminLink;
    private Periode validPeriod, invalidPeriod;
    private TerminResource validTerminResource, invalidTerminResource;
    private Date currentTime;

    @BeforeEach
    void setUp() {
        terminService = new TerminService(gyldighetsperiodeService, terminResourceCache);
        currentTime = Date.from(Instant.now());

        undervisningsgruppeResource = new UndervisningsgruppeResource();
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi("123");
        undervisningsgruppeResource.setSystemId(identifikator);

        validTerminLink = new Link("https://example.com/termin/1");

        validTerminResource = new TerminResource();
        validPeriod = getPeriod(10, 10);
        validTerminResource.setGyldighetsperiode(validPeriod);

        invalidTerminLink = new Link("https://example.com/termin/2");

        invalidTerminResource = new TerminResource();
        invalidPeriod = getPeriod(10, -5);
        invalidTerminResource.setGyldighetsperiode(invalidPeriod);
    }

    @Test
    void getUndervisningsgruppeRoleStatus_NoTerminLinks_ReturnsActive() {
        RoleStatus status = terminService.getUndervisningsgruppeRoleStatus(undervisningsgruppeResource, currentTime);

        assertThat(status.status()).isEqualTo("ACTIVE");
        assertThat(status.statusChanged()).isNull();
    }

    @Test
    void getUndervisningsgruppeRoleStatus_ValidTermin_ReturnsActiveAndDate() {
        undervisningsgruppeResource.addTermin(validTerminLink);
        undervisningsgruppeResource.addTermin(invalidTerminLink);

        when(terminResourceCache.getOptional(validTerminLink.toString())).thenReturn(Optional.of(validTerminResource));
        when(gyldighetsperiodeService.isValid(validPeriod, currentTime)).thenReturn(true);

        RoleStatus status = terminService.getUndervisningsgruppeRoleStatus(undervisningsgruppeResource, currentTime);

        assertThat(status.status()).isEqualTo("ACTIVE");
        assertThat(status.statusChanged()).isEqualTo(validPeriod.getStart());
    }

    @Test
    void getUndervisningsgruppeRoleStatus_InvalidTermin_ReturnsInactive() {
        undervisningsgruppeResource.addTermin(validTerminLink);

        when(terminResourceCache.getOptional(anyString())).thenReturn(Optional.of(validTerminResource));
        when(gyldighetsperiodeService.isValid(any(Periode.class), any(Date.class))).thenReturn(false);

        RoleStatus status = terminService.getUndervisningsgruppeRoleStatus(undervisningsgruppeResource, currentTime);

        assertThat(status.status()).isEqualTo("INACTIVE");
        assertThat(status.statusChanged()).isNull();
    }

    @Test
    void hasValidPeriod_ValidTermin_ReturnsTrue() {
        when(terminResourceCache.getOptional(validTerminLink.toString())).thenReturn(Optional.of(validTerminResource));
        when(gyldighetsperiodeService.isValid(validPeriod, currentTime)).thenReturn(true);;

        boolean result = terminService.hasValidPeriod(List.of(validTerminLink), currentTime);

        assertThat(result).isTrue();
    }

    @Test
    void hasValidPeriod_InvalidTermin_ReturnsFalse() {

        when(terminResourceCache.getOptional(invalidTerminLink.toString())).thenReturn(Optional.of(invalidTerminResource));
        when(gyldighetsperiodeService.isValid(invalidPeriod, currentTime)).thenReturn(false);
        //when(gyldighetsperiodeService.isValid(validPeriod, currentTime)).thenReturn(true);

        boolean result = terminService.hasValidPeriod(List.of(invalidTerminLink), currentTime);

        assertThat(result).isFalse();
    }

}