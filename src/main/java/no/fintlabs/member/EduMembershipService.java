package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.role.ElevforholdService;
import no.fintlabs.role.SkoleService;
import no.fintlabs.role.UndervisningsgruppeService;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import no.fintlabs.utils.MembershipUtils;
import org.springframework.stereotype.Service;

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


    public EduMembershipService(
            SkoleService skoleService,
            UndervisningsgruppeService undervisningsgruppeService,
            ElevforholdService elevforholdService,
            UserService userService, MembershipService membershipService
    ) {
        this.skoleService = skoleService;
        this.undervisningsgruppeService = undervisningsgruppeService;
        this.elevforholdService = elevforholdService;
        this.userService = userService;
        this.membershipService = membershipService;
    }
    public List<Membership> createSkoleMembershipList (
            SkoleResource skoleResource,
            Date currentTime
    ){
        log.debug("Creating Membership list for skole {} ({})"
                , skoleResource.getNavn()
                ,skoleResource.getSkolenummer().getIdentifikatorverdi());

        return skoleService.getAllElevforhold(skoleResource)
                .stream()
                .map(elevforholdResource -> createSchoolMembership(elevforholdResource, currentTime))
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
        return undervisningsgruppeService.getAllGruppemedlemskap(undervisningsgruppeResource, currentTime)
                .stream()
                .map(undervisningsgruppemedlemskapResource -> createStudyGroupMembership(undervisningsgruppemedlemskapResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(Membership -> {
                    log.debug("Membership with memberid {} has status {}",
                            Membership.getMemberId(), Membership.getMemberStatus());
                } )
                .toList();
    }

    private Optional<Membership>  createSchoolMembership(ElevforholdResource elevforholdResource, Date currentTime) {
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
            return Optional.of(membershipService.CreateMembership(user.get(), userStatus, user.get().getStatusChanged()));
        }
        MembershipStatus elevforholdStatus = MembershipUtils.getElevforholdStatus(elevforholdResource, currentTime);

        return Optional.of(membershipService.CreateMembership(user.get(), elevforholdStatus.status(),elevforholdStatus.statusChanged()));
    }

    private Optional<Membership> createStudyGroupMembership(UndervisningsgruppemedlemskapResource undervisningsgruppemedlemskapResource, Date currentTime) {
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
            return Optional.of(membershipService.CreateMembership(user.get(), userStatus, user.get().getStatusChanged()));
        }
        MembershipStatus elevforholdStatus = MembershipUtils.getElevforholdStatus(elevforholdResource.get(), currentTime);
        MembershipStatus gruppemedlemskapStatus = MembershipUtils.getUndervisningsgruppemedlemskapsStatus(undervisningsgruppemedlemskapResource, currentTime);

        if (elevforholdStatus.status().equals("INACTIVE")) {
            Date statusChanged = getStatusChanged(undervisningsgruppemedlemskapResource, elevforholdStatus, gruppemedlemskapStatus);

            return Optional.of(membershipService.CreateMembership(user.get(), elevforholdStatus.status(), statusChanged));
        }

        return Optional.of(membershipService.CreateMembership(user.get(), gruppemedlemskapStatus.status(), gruppemedlemskapStatus.statusChanged()));
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
