package no.fintlabs.member;

import no.fintlabs.role.SkoleService;
import no.fintlabs.role.BasisgruppeService;
import no.fintlabs.role.BasisgruppemedlemskapService;
import no.fintlabs.role.ElevforholdService;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevResources;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.user.UserService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class EduMemberService {
    private final SkoleService skoleService;
    private final BasisgruppeService basisgruppeService;
    private final ElevforholdService elevforholdService;
    private final UserService userService;

    public EduMemberService(
            SkoleService skoleService,
            BasisgruppeService basisgruppeService,
            ElevforholdService elevforholdService,
            UserService userService
    ) {
        this.skoleService = skoleService;
        this.basisgruppeService = basisgruppeService;
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
                .map(elevforholdResource -> elevforholdService.getElev(elevforholdResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .forEach(elevResources::addResource);

        return elevResources.getContent()
                .stream()
                .map(resource -> ResourceLinkUtil.getSelfLinkOfKind(resource, "elevnummer"))
                .map(href -> userService.getMember(href))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
    public List<Member> createBasisgruppeMemberList (
            BasisgruppeResource basisgruppeResource,
            Date currentTime
    ){
        ElevResources elevResources = new ElevResources();

        basisgruppeService.getAllElevforhold(basisgruppeResource, currentTime)
                .stream()
                .map(elevforholdResource -> elevforholdService.getElev(elevforholdResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .forEach(elevResources::addResource);

        return elevResources.getContent()
                .stream()
                .map(ElevResource::getElevnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(href ->  userService.getMember(href))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
