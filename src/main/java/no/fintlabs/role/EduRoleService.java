package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.member.EduMemberService;
import no.fintlabs.member.Member;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EduRoleService {
    private final EduMemberService eduMemberService;
    private final OrganisasjonselementService organisasjonselementService;
    private final SkoleService skoleService;
    private final RoleService roleService;

    public EduRoleService(
            EduMemberService eduMemberService,
            OrganisasjonselementService organisasjonselementService,
            SkoleService skoleService,
            RoleService roleService
    ) {
        this.eduMemberService = eduMemberService;
        this.organisasjonselementService = organisasjonselementService;
        this.skoleService = skoleService;
        this.roleService = roleService;
    }

    public Optional<Role> createOptionalSkoleRole(SkoleResource skoleResource, Date currentTime) {
        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(skoleResource);

        if (organisasjonselementResource.isEmpty()) {
            return Optional.empty();
        }
        Optional<List<Member>> members = Optional.ofNullable(eduMemberService.createSkoleMemberList(skoleResource, currentTime));

        return  Optional.of(
                createSkoleRole(skoleResource,
                        organisasjonselementResource.get(),
                        members.orElseGet(ArrayList::new))
        );
    }
    private Role createSkoleRole (
            SkoleResource skoleResource,
            OrganisasjonselementResource organisasjonselementResource,
            List<Member> members
    ) {
        String roleType = RoleType.ELEV.getRoleType();

        return getEducationRole(
                organisasjonselementResource,
                members,
                roleType,
                skoleResource.getNavn(),
                RoleSubType.SKOLEGRUPPE.getRoleSubType(),
                roleService.createSkoleRoleId(skoleResource, roleType),
                ResourceLinkUtil.getFirstSelfLink(skoleResource)
        );
    }
    public Optional<Role> createOptionalUndervisningsgruppeRole(UndervisningsgruppeResource undervisningsgruppeResource, Date currentTime) {

        Optional<SkoleResource> optionalSkole = skoleService.getSkole(undervisningsgruppeResource);

        if (optionalSkole.isEmpty()) {
            return Optional.empty();
        }
        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(optionalSkole.get());

        if (organisasjonselementResource.isEmpty()) {
                    return Optional.empty();
        }
        Optional<List<Member>> members = Optional.ofNullable(eduMemberService.createUndervisningsgruppeMemberList(undervisningsgruppeResource, currentTime));
        return  Optional.of(
                createUndervisningsgruppeRole(undervisningsgruppeResource,
                        organisasjonselementResource.get(),
                        members.orElseGet(ArrayList::new))
        );
    }
    private Role createUndervisningsgruppeRole(
            UndervisningsgruppeResource undervisningsgruppeResource,
            OrganisasjonselementResource organisasjonselementResource,
            List<Member> members
    ) {
        String roleType = RoleType.ELEV.getRoleType();

        return getEducationRole(
                organisasjonselementResource,
                members,
                roleType,
                undervisningsgruppeResource.getNavn(),
                RoleSubType.UNDERVISNINGSGRUPPE.getRoleSubType(),
                roleService.createUndervisningsgruppeRoleId(undervisningsgruppeResource, roleType),
                ResourceLinkUtil.getFirstSelfLink(undervisningsgruppeResource)
        );
    }

    private Role getEducationRole(
            OrganisasjonselementResource organisasjonselementResource,
            List<Member> members,
            String roleType,
            String groupName,
            String subRoleType,
            String roleId,
            String selfLink
    ) {
        String organizationUnitId = organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi();
        String organizationUnitName = organisasjonselementResource.getNavn();

        return Role
                .builder()
                .roleId(roleId)
                .resourceId(selfLink)
                .roleName(roleService.createRoleName(groupName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(subRoleType)
                .aggregatedRole(false)
                .organisationUnitId(organizationUnitId)
                .organisationUnitName(organizationUnitName)
                .members(members)
                .noOfMembers(members.size())
                .build();
    }

}
