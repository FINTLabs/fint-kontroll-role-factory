package no.fintlabs.utils;


import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.links.ResourceLinkUtil;

import java.util.Date;
import java.util.Optional;
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public class MembershipUtils {

    public static String getArbeidsforholdStatus(ArbeidsforholdResource arbeidsforholdResource, Date currentTime) {
        Periode gyldighetsperiode = arbeidsforholdResource.getArbeidsforholdsperiode() != null
                ? arbeidsforholdResource.getArbeidsforholdsperiode()
                : arbeidsforholdResource.getGyldighetsperiode();

        Optional<String> arbeidsforholdType = ResourceLinkUtil.getOptionalFirstLink(arbeidsforholdResource::getArbeidsforholdstype);

        if (arbeidsforholdType.isPresent() && ResourceLinkUtil.getValueFromHref(arbeidsforholdType.get()).equals("S")) {
            return "INACTIVE";
        }
        return PeriodeUtils.getStatus(gyldighetsperiode, currentTime);
    }

    public static String getElevforholdStatus(ElevforholdResource elevforholdResource, Date currentTime) {
        Periode gyldighetsperiode = elevforholdResource.getGyldighetsperiode();
        log.debug("Elevforhold gyldighetsperiode: {}, elevforholdId: {}", gyldighetsperiode, elevforholdResource.getSystemId().getIdentifikatorverdi());
        return PeriodeUtils.getStatus(gyldighetsperiode, currentTime);
    }

    public static String getUndervisningsgruppemedlemskapsStatus(UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskap, Date currentTime) {
        Periode gyldighetsperiode = undervisningsgruppemedlemskap.getGyldighetsperiode();
        log.debug("Undervisningsgruppemedlemskap gyldighetsperiode: {}, undervisningsgruppemedlemskapId: {}", gyldighetsperiode, undervisningsgruppemedlemskap.getSystemId().getIdentifikatorverdi());
        return PeriodeUtils.getStatus(gyldighetsperiode, currentTime);
    }
}

