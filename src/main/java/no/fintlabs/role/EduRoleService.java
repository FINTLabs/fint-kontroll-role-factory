package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.member.EduMemberService;
import no.fintlabs.member.Member;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.utils.RoleUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EduRoleService {
    private final OrganisasjonselementService organisasjonselementService;
    private final SkoleService skoleService;
    private final RoleService roleService;

    public EduRoleService(OrganisasjonselementService organisasjonselementService, SkoleService skoleService, RoleService roleService) {
        this.organisasjonselementService = organisasjonselementService;
        this.skoleService = skoleService;
        this.roleService = roleService;
    }

    public Optional<Role> createOptionalSkoleRole(SkoleResource skoleResource) {

        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(skoleResource);

        if (organisasjonselementResource.isEmpty()) {
            log.warn("No organisasjonselement found for skole {}. Skipping role creation", skoleResource.getNavn());
            return Optional.empty();
        }
        return  Optional.of(
                createSkoleRole(skoleResource,
                        organisasjonselementResource.get())
        );
    }
    private Role createSkoleRole (
            SkoleResource skoleResource,
            OrganisasjonselementResource organisasjonselementResource
    ) {
        log.info("Creating role for skole {} with status ACTIVE (skole roles are always active)", skoleResource.getNavn());
        String roleStatus = "ACTIVE";
        String roleType = RoleType.ELEV.getRoleType();

        return getEducationRole(
                organisasjonselementResource,
                roleType,
                roleStatus,
                skoleResource.getNavn(),
                RoleSubType.SKOLEGRUPPE.getRoleSubType(),
                roleService.createSkoleRoleId(skoleResource, roleType),
                ResourceLinkUtil.getFirstSelfLink(skoleResource),
                organisasjonselementResource.getGyldighetsperiode().getStart(),
                organisasjonselementResource.getGyldighetsperiode().getSlutt()
        );
    }
    public Optional<Role> createOptionalUndervisningsgruppeRole(
            UndervisningsgruppeResource undervisningsgruppeResource,
            Date currentTime
    ) {
        Optional<SkoleResource> optionalSkole = skoleService.getSkole(undervisningsgruppeResource);

        if (optionalSkole.isEmpty()) {
            log.warn("No skole found for undervisningsgruppe {} (systemid {}). Skipping role creation",
                    undervisningsgruppeResource.getNavn(),
                    undervisningsgruppeResource.getSystemId().getIdentifikatorverdi());
            return Optional.empty();
        }
        SkoleResource skoleResource = optionalSkole.get();

        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(skoleResource);

        if (organisasjonselementResource.isEmpty()) {
            log.warn("No organisasjonselement found for skole {}. Skipping role creation for undervisningsgruppe {}",
                    skoleResource.getNavn(),
                    undervisningsgruppeResource.getNavn()
            );
            return Optional.empty();
        }
        return  Optional.of(
                createUndervisningsgruppeRole(undervisningsgruppeResource,
                        organisasjonselementResource.get(),
                        currentTime)
        );
    }
    private Role createUndervisningsgruppeRole(
            UndervisningsgruppeResource undervisningsgruppeResource,
            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime
    ) {
        log.info("Creating role for undervisningsgruppe {}",
                undervisningsgruppeResource.getNavn()
        );
        String roleType = RoleType.ELEV.getRoleType();
        String roleStatus = RoleUtils.getUndervisningsgruppeRoleStatus(undervisningsgruppeResource, currentTime);

        return getEducationRole(
                organisasjonselementResource,
                roleType,
                roleStatus,
                undervisningsgruppeResource.getNavn(),
                RoleSubType.UNDERVISNINGSGRUPPE.getRoleSubType(),
                roleService.createUndervisningsgruppeRoleId(undervisningsgruppeResource, roleType),
                ResourceLinkUtil.getFirstSelfLink(undervisningsgruppeResource),
                undervisningsgruppeResource.getPeriode().getFirst().getStart(),
                undervisningsgruppeResource.getPeriode().getFirst().getSlutt()
        );
    }

    private Role getEducationRole(
            OrganisasjonselementResource organisasjonselementResource,
            String roleType,
            String roleStatus,
            String groupName,
            String subRoleType,
            String roleId,
            String selfLink,
            Date startDate,
            Date endDate
    ) {
        String organizationUnitId = organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi();
        String organizationUnitName = organisasjonselementResource.getNavn();
        String schoolShortName = organizationUnitName.split(" ")[0];

        return Role
                .builder()
                .roleId(roleId)
                .resourceId(selfLink)
                .roleStatus(roleStatus)
                .roleName(roleService.createSchoolRoleName(groupName, schoolShortName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(RoleUserType.STUDENT.name())
                .roleSubType(subRoleType)
                .aggregatedRole(false)
                .organisationUnitId(organizationUnitId)
                .organisationUnitName(organizationUnitName)
                .startDate(organisasjonselementResource.getGyldighetsperiode().getStart())
                .endDate(organisasjonselementResource.getGyldighetsperiode().getSlutt())
                .build();
    }

}
