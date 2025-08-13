package no.fintlabs.utils;


import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.membership.MembershipStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class MembershipUtils {

    private static int DAYS_BEFORE_START_STUDENT;
    @Value("${fint.kontroll.member.days-before-start-student}")
    private void setDaysBeforeStartStudent(int daysBeforeStartStudent) {
        MembershipUtils.DAYS_BEFORE_START_STUDENT = daysBeforeStartStudent;
    };

    public static MembershipStatus getArbeidsforholdStatus(ArbeidsforholdResource arbeidsforholdResource, Date currentTime) {
        Periode gyldighetsperiode = arbeidsforholdResource.getArbeidsforholdsperiode() != null
                ? arbeidsforholdResource.getArbeidsforholdsperiode()
                : arbeidsforholdResource.getGyldighetsperiode();

        Optional<String> arbeidsforholdType = ResourceLinkUtil.getOptionalFirstLink(arbeidsforholdResource::getArbeidsforholdstype);

        if (arbeidsforholdType.isPresent() && ResourceLinkUtil.getValueFromHref(arbeidsforholdType.get()).equals("S")) {
            //TODO maybe this should be another status and the arbeidsforholdtypes to check against should be a list from config
            return new MembershipStatus("INACTIVE", null);
        }
        return new MembershipStatus(
                PeriodeUtils.getStatus(gyldighetsperiode, currentTime, 0),
                PeriodeUtils.getStatusChanged(gyldighetsperiode, currentTime, 0)
        );
    }

    public static MembershipStatus getElevforholdStatus(ElevforholdResource elevforholdResource, Date currentTime) {
        Periode gyldighetsperiode = elevforholdResource.getGyldighetsperiode();

        return new MembershipStatus(
                PeriodeUtils.getStatus(gyldighetsperiode, currentTime, DAYS_BEFORE_START_STUDENT),
                PeriodeUtils.getStatusChanged(gyldighetsperiode, currentTime,  DAYS_BEFORE_START_STUDENT)
        );
    }
    public static MembershipStatus getUndervisningsgruppemedlemskapsStatus(UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskap, Date currentTime) {
        Periode gyldighetsperiode = undervisningsgruppemedlemskap.getGyldighetsperiode();

        return new MembershipStatus(
                PeriodeUtils.getStatus(gyldighetsperiode, currentTime, DAYS_BEFORE_START_STUDENT),
                PeriodeUtils.getStatusChanged(gyldighetsperiode, currentTime, DAYS_BEFORE_START_STUDENT)
        );
    }
}

