package no.fintlabs.membership;


import lombok.extern.slf4j.Slf4j;
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
    public List<Membership> createSkoleMembershipList (
            SkoleResource skoleResource,
            Date currentTime
    ){
        log.debug("Creating Membership list for skole {} ({})"
                , skoleResource.getNavn()
                ,skoleResource.getSkolenummer().getIdentifikatorverdi());

        String roleId = roleService.createSkoleRoleId(skoleResource, RoleType.ELEV.getRoleType());

        Optional<RoleCatalogRole> role = roleService.getRoleCatalogRole(roleId);
        if (role.isEmpty()) {
            return new ArrayList<>();
        }

        return skoleService.getAllElevforhold(skoleResource)
                .stream()
                .map(elevforholdResource -> createSchoolMembership(role.get(), elevforholdResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(Membership -> {
                    log.debug("Membership with memberid {} has status {}",
                            Membership.getMemberId(), Membership.getMemberStatus());
                } )
                .toList();

    }
    public List<Membership> createUndervisningsgruppeMembershipList (
            UndervisningsgruppeResource undervisningsgruppeResource,
            Date currentTime
    ){
        String roleId = roleService.createUndervisningsgruppeRoleId(undervisningsgruppeResource, RoleType.ELEV.getRoleType());

        Optional<RoleCatalogRole> role = membershipService.roleService.getRoleCatalogRole(roleId);

        if (role.isEmpty()) {
            return new ArrayList<>();
        }
        return undervisningsgruppeService.getAllGruppemedlemskap(undervisningsgruppeResource, currentTime)
                .stream()
                .map(undervisningsgruppemedlemskapResource -> createStudyGroupMembership(role.get(), undervisningsgruppemedlemskapResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(Membership -> {
                    log.debug("Membership with memberid {} has status {}",
                            Membership.getMemberId(), Membership.getMemberStatus());
                } )
                .toList();
    }

    private Optional<Membership>  createSchoolMembership(RoleCatalogRole role, ElevforholdResource elevforholdResource, Date currentTime) {
        Optional<ElevResource> elevResource = elevforholdService.getElev(elevforholdResource);

        if (elevResource.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> user = userService.getUser(elevResource.get().getElevnummer().getIdentifikatorverdi());

        if (user.isEmpty()) {
            return Optional.empty();
        }
        String userStatus = user.get().getStatus();

        if (userStatus != null && !userStatus.equals("ACTIVE")) {
            return Optional.of(membershipService.createMembership(role, user.get(), userStatus, user.get().getStatusChanged()));
        }
        MembershipStatus elevforholdStatus = MembershipUtils.getElevforholdStatus(elevforholdResource, currentTime);

        return Optional.of(membershipService.createMembership(role, user.get(), elevforholdStatus.status(),elevforholdStatus.statusChanged()));
    }

    private Optional<Membership> createStudyGroupMembership(
            RoleCatalogRole role,
            UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskapResource,
            Date currentTime) {
        Optional<ElevforholdResource> elevforholdResource = elevforholdService.getElevforhold(undervisningsgruppemedlemskapResource);

        if (elevforholdResource.isEmpty()) {
            return Optional.empty();
        }
        Optional<ElevResource> elevResource = elevforholdService.getElev(elevforholdResource.get());

        if (elevResource.isEmpty()) {
            return Optional.empty();
        }

        Optional<User> user = userService.getUser(elevResource.get().getElevnummer().getIdentifikatorverdi());

        if (user.isEmpty()) {
            return Optional.empty();
        }
        String userStatus = user.get().getStatus();
        if (userStatus != null && !userStatus.equals("ACTIVE")) {
            return Optional.of(membershipService.createMembership(role, user.get(), userStatus, user.get().getStatusChanged()));
        }
        MembershipStatus elevforholdStatus = MembershipUtils.getElevforholdStatus(elevforholdResource.get(), currentTime);
        MembershipStatus gruppemedlemskapStatus = MembershipUtils.getUndervisningsgruppemedlemskapsStatus(undervisningsgruppemedlemskapResource, currentTime);

        if (elevforholdStatus.status().equals("INACTIVE")) {
            Date statusChanged = getStatusChanged(undervisningsgruppemedlemskapResource, elevforholdStatus, gruppemedlemskapStatus);

            return Optional.of(membershipService.createMembership(role, user.get(), elevforholdStatus.status(), statusChanged));
        }

        return Optional.of(membershipService.createMembership(role, user.get(), gruppemedlemskapStatus.status(), gruppemedlemskapStatus.statusChanged()));
    }

    private static Date getStatusChanged(
            UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskapResource,
            MembershipStatus elevforholdStatus,
            MembershipStatus gruppemedlemskapStatus
    ) {
        Date undervisingsgruppeMedlemskapSlutt = undervisningsgruppemedlemskapResource.getGyldighetsperiode().getSlutt() != null ?
                undervisningsgruppemedlemskapResource.getGyldighetsperiode().getSlutt()
                : null;

        return undervisingsgruppeMedlemskapSlutt !=null || elevforholdStatus.statusChanged().before(undervisingsgruppeMedlemskapSlutt) ?
                elevforholdStatus.statusChanged()
                : gruppemedlemskapStatus.statusChanged();
    }

}

