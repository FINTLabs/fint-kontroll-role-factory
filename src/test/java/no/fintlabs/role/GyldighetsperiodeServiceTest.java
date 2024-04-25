package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fintlabs.base.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class GyldighetsperiodeServiceTest extends BaseTest {

    @Test
    void givenValidPeriod_isValid_return_true() {
        GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
        Periode validPeriod = getPeriod(10,10);
        assertThat(gyldighetsperiodeService.isValid(validPeriod, getCurrentTime())).isTrue();
    }
    @Test
    void givenValidPeriodNoEnddate_isValid_return_true() {
        GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
        Periode validPeriod = new Periode();
        Date startDate = Date.from((LocalDate.now().minusDays(5).atStartOfDay(ZoneId.of("Z")).toInstant()));
        validPeriod.setStart(startDate);
        assertThat(gyldighetsperiodeService.isValid(validPeriod, getCurrentTime())).isTrue();
    }
    @Test
    void givenPastPeriod_isValid_return_false() {
        GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
        Periode validPeriod = getPeriod(10,-5);
        assertThat(gyldighetsperiodeService.isValid(validPeriod, getCurrentTime())).isFalse();
    }
    @Test
    void givenFuturePeriod_isValid_return_false() {
        GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
        Periode validPeriod = getPeriod(10,-5);
        assertThat(gyldighetsperiodeService.isValid(validPeriod, getCurrentTime())).isFalse();
    }
    @Test
    void givenNullPeriod_isValid_throw_NullPeriodeException() {
        GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
        assertThrows(GyldighetsperiodeService.NullPeriodeException.class, ()-> gyldighetsperiodeService.isValid(null, getCurrentTime()));
    }
    @Test
    void givenEmptyPeriod_isValid_throw_NullPeriodeStartDatoException() {
        GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
        Periode emptyPeriod = new Periode();
        assertThrows(GyldighetsperiodeService.NullPeriodeStartDatoException.class, ()-> gyldighetsperiodeService.isValid(emptyPeriod, getCurrentTime()));
    }
}