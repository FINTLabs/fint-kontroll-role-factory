package no.fintlabs.utils;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.role.GyldighetsperiodeService;
import no.fintlabs.termin.TerminService;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
