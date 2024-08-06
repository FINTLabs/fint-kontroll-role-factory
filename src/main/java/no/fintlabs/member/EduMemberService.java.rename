package no.fintlabs.member;

import no.fintlabs.role.SkoleService;
import no.fintlabs.role.UndervisningsgruppeService;
import no.fintlabs.role.ElevforholdService;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevResources;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.user.UserService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class EduMemberService {
    private final SkoleService skoleService;
    private final UndervisningsgruppeService undervisningsgruppeService;
    private final ElevforholdService elevforholdService;
    private final UserService userService;

    public EduMemberService(
            SkoleService skoleService,
            UndervisningsgruppeService undervisningsgruppeService,
            ElevforholdService elevforholdService,
            UserService userService
    ) {
        this.skoleService = skoleService;
        this.undervisningsgruppeService = undervisningsgruppeService;
        this.elevforholdService = elevforholdService;
        this.userService = userService;
    }
    public List<Member> createSkoleMemberList (
        SkoleResource skoleResource,
        Date currentTime
    ){
        ElevResources elevResources = new ElevResources();

        skoleService.getAllValidElevforhold(skoleResource, currentTime)
                .stream()
                .map(elevforholdService::getElev)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .forEach(elevResources::addResource);

        return getMemberList(elevResources);
    }

    public List<Member> createUndervisningsgruppeMemberList (
            UndervisningsgruppeResource undervisningsgruppeResource,
            Date currentTime
    ){
        ElevResources elevResources = new ElevResources();

        undervisningsgruppeService.getValidAllElevforhold(undervisningsgruppeResource, currentTime)
                .stream()
                .map(elevforholdService::getElev)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .forEach(elevResources::addResource);

        return getMemberList(elevResources);
    }
    private List<Member> getMemberList(ElevResources elevResources) {
        return elevResources.getContent()
                .stream()
                .map(ElevResource::getElevnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(userService::getMember)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
    public List<Membership> createUndervisningsgruppeMembershipList (
            UndervisningsgruppeResource undervisningsgruppeResource,
            Date currentTime
    ){
        ElevResources elevResources = new ElevResources();

        undervisningsgruppeService.getValidAllElevforhold(undervisningsgruppeResource, currentTime)
                .stream()
                .map(elevforholdService::getElev)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .forEach(elevResources::addResource);

        return getMembershipList(elevResources);
    }
    private List<Membership> getMembershipList(ElevResources elevResources) {
        return elevResources.getContent()
                .stream()
                .map(ElevResource::getElevnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(userService::getMember)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Member::toMemberShip)
                .toList();
    }
}
