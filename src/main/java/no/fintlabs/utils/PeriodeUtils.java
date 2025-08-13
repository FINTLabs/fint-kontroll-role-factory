package no.fintlabs.utils;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fintlabs.role.GyldighetsperiodeService;

import java.util.Date;

public class PeriodeUtils {
    private static final GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
    public static String getStatus(Periode gyldighetsperiode, Date currentTime, int daysBeforeStart) {
        return gyldighetsperiodeService.isValid(gyldighetsperiode, currentTime, daysBeforeStart)
                ? "ACTIVE"
                : "INACTIVE";
    }

    public static Date getStatusChanged(Periode gyldighetsperiode, Date currentTime, int daysBeforeStart) {
        return gyldighetsperiodeService.isValid(gyldighetsperiode, currentTime, daysBeforeStart)? gyldighetsperiodeService.getStartDate(gyldighetsperiode.getStart(), daysBeforeStart) : gyldighetsperiode.getSlutt();
    }
}
