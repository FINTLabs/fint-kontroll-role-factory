package no.fintlabs.utils;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.FintObject;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fintlabs.role.RoleStatus;

import java.util.Date;
import java.util.Optional;

@Slf4j
public class RoleUtils {

    public static RoleStatus getOrgUnitRoleStatus(OrganisasjonselementResource roleResource, Date currentTime) {
        Optional< Periode> gyldighetsperiode = Optional.ofNullable(roleResource.getGyldighetsperiode());

        if (gyldighetsperiode.isEmpty()) {
            log.warn("No gyldighetsperiode found for org unit {}. Status for role is set to ACTIVE",
                    roleResource.getOrganisasjonsId().getIdentifikatorverdi()
            );
            return new RoleStatus("ACTIVE", null);
        }
        log.info("Org unit role for org unit {} has status {}",
                roleResource.getOrganisasjonsId().getIdentifikatorverdi(),
                PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime)
        );
        return new RoleStatus(
                PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime),
                PeriodeUtils.getStatusChanged(gyldighetsperiode.get(), currentTime)
        );
    }
    public static RoleStatus getUndervisningsgruppeRoleStatus(UndervisningsgruppeResource undervisningsgruppeResource, Date currentTime) {
        Optional<Periode> gyldighetsperiode = Optional.ofNullable(undervisningsgruppeResource.getPeriode().get(0));

        if (gyldighetsperiode.isEmpty()) {
            log.warn("No gyldighetsperiode found for undervisningsgruppe {}. Status for role is set to ACTIVE",
                    undervisningsgruppeResource.getSystemId().getIdentifikatorverdi()
            );
            return new RoleStatus("ACTIVE", null);
        }
        log.info("Undervisningsgruppe role for undervisningsgruppe {} has status {}",
                undervisningsgruppeResource.getSystemId().getIdentifikatorverdi(),
                PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime)
        );
        return new RoleStatus(
                PeriodeUtils.getStatus(gyldighetsperiode.get(), currentTime),
                PeriodeUtils.getStatusChanged(gyldighetsperiode.get(), currentTime)
        );
    }
}
