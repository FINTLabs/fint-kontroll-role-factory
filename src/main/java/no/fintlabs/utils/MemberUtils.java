package no.fintlabs.utils;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fintlabs.role.GyldighetsperiodeService;

import java.util.Date;

public class MemberUtils {
    private static final GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();

    public MemberUtils(GyldighetsperiodeService gyldighetsperiodeService){

    }

    public static String getMemberStatus(ArbeidsforholdResource arbeidsforholdResource, Date currentTime) {
        Periode gyldighetsperiode = arbeidsforholdResource.getArbeidsforholdsperiode() != null
                ? arbeidsforholdResource.getArbeidsforholdsperiode()
                : arbeidsforholdResource.getGyldighetsperiode();

        return gyldighetsperiodeService.isValid(gyldighetsperiode, currentTime)
                ? "ACTIVE"
                : "INACTIVE";
    }
}
