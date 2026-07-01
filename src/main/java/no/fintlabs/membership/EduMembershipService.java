package no.fintlabs.membership;


import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.role.*;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import no.fintlabs.utils.MembershipUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EduMembershipService {
    private final SkoleService skoleService;
    private final UndervisningsgruppeService undervisningsgruppeService;
    private final ElevforholdService elevforholdService;
    private final UserService userService;
    private final MembershipService membershipService;
    private final RoleService roleService;


    public EduMembershipService(
            SkoleService skoleService,
            UndervisningsgruppeService undervisningsgruppeService,
            ElevforholdService elevforholdService,
            UserService userService,
            MembershipService membershipService,
            RoleService roleService) {
        this.skoleService = skoleService;
        this.undervisningsgruppeService = undervisningsgruppeService;
        this.elevforholdService = elevforholdService;
        this.userService = userService;
        this.membershipService = membershipService;
        this.roleService = roleService;
    }

    private static void logMembershipDetails(Membership membership) {
        log.info("Membership with role id {}, memberid {} and status {} added to list",
                membership.getRoleId(),
                membership.getMemberId(),
                membership.getMemberStatus());
    }

    private static boolean hasGyldighetsperiode(ElevforholdResource elevforholdResource) {
        if (elevforholdResource.getGyldighetsperiode() != null) {
            return true;
        }

        log.warn("Elevforhold {} has no gyldighetsperiode, no membership added",
                elevforholdResource.getSystemId().getIdentifikatorverdi()
        );
        return false;
    }

    public List<Membership> createSkoleMembershipList(
            SkoleResource skoleResource,
            Date currentTime
    ) {
        String roleId = roleService.createSkoleRoleId(skoleResource, RoleType.ELEV.getRoleType());

        log.debug("Creating Membership list for skole {} ({})"
                , skoleResource.getNavn()
                , roleId);

        Optional<RoleCatalogRole> role = roleService.getRoleCatalogRole(roleId);
        if (role.isEmpty()) {
            return new ArrayList<>();
        }

        return skoleService.getAllElevforhold(skoleResource)
                .stream()
                .filter(EduMembershipService::hasGyldighetsperiode)
                .map(elevforholdResource -> createSchoolMembership(role.get(), elevforholdResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(EduMembershipService::logMembershipDetails)
                .toList();
    }

    public List<Membership> createUndervisningsgruppeMembershipList(
            UndervisningsgruppeResource undervisningsgruppeResource,
            Date currentTime
    ) {
        String roleId = roleService.createUndervisningsgruppeRoleId(undervisningsgruppeResource, RoleType.ELEV.getRoleType());

        Optional<RoleCatalogRole> roleCatalogRole = roleService.getRoleCatalogRole(roleId);

        if (roleCatalogRole.isEmpty()) {
            return List.of();
        }
        return undervisningsgruppeService.getAllGruppemedlemskap(undervisningsgruppeResource)
                .stream()
                .map(undervisningsgruppemedlemskapResource -> createStudyGroupMembership(roleCatalogRole.get(), undervisningsgruppemedlemskapResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(EduMembershipService::logMembershipDetails)
                .toList();
    }

    private Optional<Membership> createSchoolMembership(
            RoleCatalogRole roleCatalogRole,
            ElevforholdResource elevforholdResource,
            Date currentTime
    ) {

        log.info("Trying to create skole membership for role {} and elevforhold {}",
                roleCatalogRole.getRoleId(),
                elevforholdResource.getSystemId().getIdentifikatorverdi()
        );

        if (roleCatalogRole.getRoleStatus() == null) {
            log.warn("Role catalog role found, but role status not found for role {}. Skole role membership not created",
                    roleCatalogRole.getId()
            );
            return Optional.empty();
        }
        Optional<ElevResource> elevResource = elevforholdService.getElev(elevforholdResource);

        if (elevResource.isEmpty() || elevResource.get().getElevnummer() == null) {
            return Optional.empty();
        }
        Optional<User> user = userService.getUser(elevResource.get().getElevnummer().getIdentifikatorverdi());

        if (user.isEmpty()) {
            return Optional.empty();
        }
        User member = user.get();
        Date startDate = elevforholdResource.getGyldighetsperiode().getStart();
        Date endDate = elevforholdResource.getGyldighetsperiode().getSlutt();
        Optional<String> userStatus = Optional.ofNullable(member.getStatus());

        if (userStatus.isPresent() && !userStatus.get().equals("ACTIVE")) {
            return Optional.of(
                    membershipService.createMembership(
                            roleCatalogRole,
                            member,
                            userStatus.get(),
                            startDate,
                            endDate
                    )
            );
        }
        MembershipStatus elevforholdStatus = MembershipUtils.getElevforholdStatus(elevforholdResource, currentTime);

        return Optional.of(
                membershipService.createMembership(
                        roleCatalogRole,
                        member,
                        elevforholdStatus.status(),
                        startDate,
                        endDate
                )
        );
    }

    private Optional<Membership> createStudyGroupMembership(
            RoleCatalogRole roleCatalogRole,
            UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskapResource,
            Date currentTime
    ) {
        log.info("Trying to create undervisningsgruppe membership for role {} and gruppemedlemskap {}",
                roleCatalogRole.getRoleId(),
                undervisningsgruppemedlemskapResource.getSystemId().getIdentifikatorverdi()
        );

        if (roleCatalogRole.getRoleStatus() == null) {
            log.warn("Role catalog role found, but role status not found for role {}. Undervisningsgruppe membership for role {} not created",
                    roleCatalogRole.getRoleId(),
                    roleCatalogRole.getRoleId()
            );
            return Optional.empty();
        }
        Optional<ElevforholdResource> elevforholdResource = elevforholdService.getElevforhold(undervisningsgruppemedlemskapResource);

        if (elevforholdResource.isEmpty()) {
            Optional<Link> elevforholdLink = undervisningsgruppemedlemskapResource.getElevforhold().stream().findFirst();

            if (elevforholdLink.isPresent()) {
                log.warn("Elevforhold {} referenced by but not found for gruppemedlemskap {}. Undervisningsgruppe membership for role {} not created",
                        elevforholdLink.get().getHref(),
                        undervisningsgruppemedlemskapResource.getSystemId().getIdentifikatorverdi(),
                        roleCatalogRole.getRoleId()
                );
            } else {
                log.warn("No elevforhold referenced by gruppemedlemskap {}. Undervisningsgruppe membership for role {} not created",
                        undervisningsgruppemedlemskapResource.getSystemId().getIdentifikatorverdi(),
                        roleCatalogRole.getRoleId()
                );
            }
            return Optional.empty();
        }
        ElevforholdResource elevforhold = elevforholdResource.get();

        if (elevforhold.getGyldighetsperiode() == null) {
            log.warn("Elevforhold {} has no gyldighetsperiode. Undervisningsgruppe membership for role {} not created",
                    elevforhold.getSystemId().getIdentifikatorverdi(),
                    roleCatalogRole.getRoleId()
            );
            return Optional.empty();
        }
        Optional<ElevResource> elevResource = elevforholdService.getElev(elevforhold);

        if (elevResource.isEmpty() || elevResource.get().getElevnummer() == null) {
            return Optional.empty();
        }
        Optional<User> user = userService.getUser(elevResource.get().getElevnummer().getIdentifikatorverdi());

        if (user.isEmpty()) {
            return Optional.empty();
        }
        User member = user.get();
        Date startDate = undervisningsgruppemedlemskapResource.getGyldighetsperiode().getStart();
        Date endDate = undervisningsgruppemedlemskapResource.getGyldighetsperiode().getSlutt();

        if (roleCatalogRole.getRoleStatus().equals("INACTIVE")) {
            log.info("Role {} is INACTIVE. Membership status for member {} is set to INACTIVE",
                    roleCatalogRole.getRoleId(),
                    member.getId()
            );
            return Optional.of(
                    membershipService.createMembership(
                            roleCatalogRole,
                            member,
                            "INACTIVE",
                            startDate,
                            endDate
                    )
            );
        }
        String userStatus = member.getStatus();
        if (userStatus != null && !userStatus.equals("ACTIVE")) {
            return Optional.of(
                    membershipService.createMembership(
                            roleCatalogRole,
                            member,
                            userStatus,
                            startDate,
                            endDate
                    )
            );
        }
        MembershipStatus elevforholdStatus = MembershipUtils.getElevforholdStatus(elevforhold, currentTime);
        MembershipStatus gruppemedlemskapStatus = MembershipUtils.getUndervisningsgruppemedlemskapsStatus(undervisningsgruppemedlemskapResource, currentTime);

        if (elevforholdStatus.status().equals("INACTIVE")) {
            return Optional.of(
                    membershipService.createMembership(
                            roleCatalogRole,
                            member,
                            elevforholdStatus.status(),
                            startDate,
                            endDate
                    )
            );
        }

        return Optional.of(
                membershipService.createMembership(
                        roleCatalogRole,
                        member,
                        gruppemedlemskapStatus.status(),
                        startDate,
                        endDate
                )
        );
    }

}
