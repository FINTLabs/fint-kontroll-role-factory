package no.fintlabs.member;

public class EduMemberService {
//    private final SkoleService skoleService;
//    private final BasisgruppeService basisgruppeService;
//    private final BasisgruppemedlemskapService basisgruppemedlemskapService;
//    private final ElevforholdService elevforholdService;

//            SkoleService skoleService,
//            BasisgruppeService basisgruppeService,
//            BasisgruppemedlemskapService basisgruppemedlemskapService,
//            ElevforholdService elevforholdService,

    //        this.skoleService = skoleService;
//        this.basisgruppeService = basisgruppeService;
//        this.basisgruppemedlemskapService = basisgruppemedlemskapService;
//        this.elevforholdService = elevforholdService

    //    public List<Member> createSkoleMemberList (
//            SkoleResource skoleResource,
//            Date currentTime)
//    {
//        ElevResources elevResources = new ElevResources();
//
//        skoleService.getAllValidElevforhold(skoleResource, currentTime)
//                .stream()
//                .map(elevforholdResource -> elevforholdService.getElev(elevforholdResource))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList()
//                .forEach(elevResources::addResource);
//
//        return elevResources.getContent()
//                .stream()
//                .map(resource -> ResourceLinkUtil.getSelfLinkOfKind(resource,"elevnummer"))
//                .map(href -> getMember(href))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//
//    }

//    public List<Member> createBasisgruppeMemberList (
//            BasisgruppeResource basisgruppeResource
//    ){
//        ElevResources elevResources = new ElevResources();
//
//        basisgruppeService.getAllElevforhold(basisgruppeResource)
//                .stream()
//                .map(elevforholdResource -> elevforholdService.getElev(elevforholdResource))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList()
//                .forEach(elevResources::addResource);
//
//        return elevResources.getContent()
//                .stream()
//                .map(ElevResource::getElevnummer)
//                .map(Identifikator::getIdentifikatorverdi)
//                .map(href -> getMember(href))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//    }
}
