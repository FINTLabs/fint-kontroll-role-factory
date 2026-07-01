package no.fintlabs.utils;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;

import java.util.Date;
import java.util.Optional;

@Slf4j
public class RoleUtils {

    public static String getOrgUnitRoleStatus(OrganisasjonselementResource roleResource, Date currentTime) {
        Optional< Periode> gyldighetsperiode = Optional.ofNullable(roleResource.getGyldighetsperiode());

        if (gyldighetsperiode.isEmpty()) {
            log.warn("No gyldighetsperiode found for org unit {}. Status for role is set to ACTIVE",
                    roleResource.getOrganisasjonsId().getIdentifikatorverdi()
            );
            return "ACTIVE";
        }
        log.info("Org unit role for org unit {} has status {}",
                roleResource.getOrganisasjonsId().getIdentifikatorverdi(),
                PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime)
        );
        return PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime);
    }
    public static String getUndervisningsgruppeRoleStatus(UndervisningsgruppeResource undervisningsgruppeResource, Date currentTime) {
        Optional<Periode> gyldighetsperiode = undervisningsgruppeResource.getPeriode() != null ? undervisningsgruppeResource.getPeriode().stream().findFirst() : Optional.empty();

        if (gyldighetsperiode.isEmpty()) {
            log.warn("No gyldighetsperiode found for undervisningsgruppe {}. Status for role is set to ACTIVE",
                    undervisningsgruppeResource.getSystemId().getIdentifikatorverdi()
            );
            return "ACTIVE";
        }
        log.info("Undervisningsgruppe role for undervisningsgruppe {} has status {}",
                undervisningsgruppeResource.getSystemId().getIdentifikatorverdi(),
                PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime)
        );
        return PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime);
    }
}
