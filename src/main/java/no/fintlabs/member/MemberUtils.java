package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.role.GyldighetsperiodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MemberUtils {

    private static int daysBeforeArbeidsforholdStart;
    private static int daysBeforeElevforholdStart;
    private static int daysBeforeUndervisningsgruppemedlemskapStart;

    @Value("${fint.kontroll.member.days-before-arbeidsforhold-start}")
    private void setDaysBeforeArbeidsforholdStart(int daysBeforeArbeidsforholdStart) {
        MemberUtils.daysBeforeArbeidsforholdStart = daysBeforeArbeidsforholdStart;
    }
    @Value("${fint.kontroll.member.days-before-elevforhold-start}")
    private void setDaysBeforeElevforholdStart(int daysBeforeElevforholdStart) {
        MemberUtils.daysBeforeElevforholdStart = daysBeforeElevforholdStart;
    }
    @Value("${fint.kontroll.member.days-before-undervisningsgruppemedlemskap-start}")
    private void setDaysBeforeUndervisningsgruppemedlemskapStart(int daysBeforeUndervisningsgruppemedlemskapStart) {
        MemberUtils.daysBeforeUndervisningsgruppemedlemskapStart = daysBeforeUndervisningsgruppemedlemskapStart;
    }


    private static GyldighetsperiodeService gyldighetsperiodeService = new GyldighetsperiodeService();

    public MemberUtils(GyldighetsperiodeService gyldighetsperiodeService) {
        MemberUtils.gyldighetsperiodeService = gyldighetsperiodeService;
    }
    public static String getArbeidsforholdStatus(
            ArbeidsforholdResource arbeidsforholdResource,
            Date currentTime
    ) {
        Periode gyldighetsPeriode = arbeidsforholdResource.getArbeidsforholdsperiode();

        return gyldighetsperiodeService.isValid(gyldighetsPeriode, currentTime, daysBeforeArbeidsforholdStart)
                ? "ACTIVE"
                : "DISABLED";
    }

    public static String getElevforholdStatus(ElevforholdResource elevforholdResource, Date currentTime) {
        Periode gyldighetsPeriode = elevforholdResource.getGyldighetsperiode();

        return gyldighetsperiodeService.isValid(gyldighetsPeriode, currentTime, daysBeforeElevforholdStart)
                ? "ACTIVE"
                : "DISABLED";
    }
    public static String getUndervisningsgruppemedlemskapStatus(
            UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskapResource,
            Date currentTime
    ) {
        Periode gyldighetsPeriode = undervisningsgruppemedlemskapResource.getGyldighetsperiode();

        return gyldighetsperiodeService.isValid(gyldighetsPeriode, currentTime, daysBeforeUndervisningsgruppemedlemskapStart)
                ? "ACTIVE"
                : "DISABLED";
    }
}
