package no.fintlabs.utils;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fintlabs.role.GyldighetsperiodeService;

import java.util.Date;

public class PeriodeUtils {
    private static final GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();
    public static String getStatus(Periode gyldighetsperiode, Date currentTime) {
        return gyldighetsperiodeService.isValid(gyldighetsperiode, currentTime)
                ? "ACTIVE"
                : "INACTIVE";
    }

    public static Date getStatusChanged(Periode gyldighetsperiode, Date currentTime) {
        return gyldighetsperiodeService.isValid(gyldighetsperiode, currentTime)? gyldighetsperiode.getStart() : gyldighetsperiode.getSlutt();
    }
}
