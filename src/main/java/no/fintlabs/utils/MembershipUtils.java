package no.fintlabs.utils;


import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.membership.MembershipStatus;

import java.util.Date;
import java.util.Optional;

public class MembershipUtils {

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
                PeriodeUtils.getStatus(gyldighetsperiode, currentTime),
                PeriodeUtils.getStatusChanged(gyldighetsperiode, currentTime)
        );
    }

    public static MembershipStatus getElevforholdStatus(ElevforholdResource elevforholdResource, Date currentTime) {
        Periode gyldighetsperiode = elevforholdResource.getGyldighetsperiode();

        return new MembershipStatus(
                PeriodeUtils.getStatus(gyldighetsperiode, currentTime),
                PeriodeUtils.getStatusChanged(gyldighetsperiode, currentTime)
        );
    }
    public static MembershipStatus getUndervisningsgruppemedlemskapsStatus(UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskap, Date currentTime) {
        Periode gyldighetsperiode = undervisningsgruppemedlemskap.getGyldighetsperiode();

        return new MembershipStatus(
                PeriodeUtils.getStatus(gyldighetsperiode, currentTime),
                PeriodeUtils.getStatusChanged(gyldighetsperiode, currentTime)
        );
    }
}

