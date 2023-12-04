package no.fintlabs.role;

public class EduRolePublishingComponent {
        /*
    private final FintCache<String, ElevResource> elevResourceCache;
    private final FintCache<String, ElevforholdResource> elevforholdResourceFintCache;
    private final FintCache<String, SkoleressursResource> skoleressursResourceFintCache;
    private final FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache;
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache;
    private final  FintCache<String, TerminResource> terminResourceCache;
    private final FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache;
    private final BasisgruppeService basisgruppeService;
    private final BasisgruppemedlemskapService basisgruppemedlemskapService;
    private final ElevforholdService elevforholdService;
        private final SkoleService skoleService;

        this.elevResourceCache = elevResourceCache;
        this.elevforholdResourceFintCache = elevforholdResourceFintCache;
        this.skoleressursResourceFintCache = skoleressursResourceFintCache;
        this.undervisningsforholdResourceFintCache = undervisningsforholdResourceFintCache;
                this.basisgruppeResourceFintCache = basisgruppeResourceFintCache;
        this.terminResourceCache = terminResourceCache;
        this.basisgruppemedlemskapResourceFintCache = basisgruppemedlemskapResourceFintCache;

        this.skoleService = skoleService;
        this.basisgruppeService = basisgruppeService;
        this.basisgruppemedlemskapService = basisgruppemedlemskapService;
        this.elevforholdService = elevforholdService;
         */


//        List<String> skolerToPublish = Arrays.asList("ALL");
//
//        List<Role> validSkoleRoles = skoleService.getAll()
//                .stream()
//                .filter(skoleResource -> skolerToPublish.contains(skoleResource.getSkolenummer().getIdentifikatorverdi())
//                || skolerToPublish.contains("ALL"))
//                .filter(skoleResource -> !skoleResource.getElevforhold().isEmpty())
//                .map(skoleResource -> createOptionalSkoleRole(skoleResource, currentTime))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//        List<Role> publishedSkoleRoles = roleEntityProducerService.publishChangedRoles(validSkoleRoles);
//
//        log.info("Published {} of {} valid skole roles", publishedSkoleRoles.size(), validSkoleRoles.size());
//        log.debug("Ids of published basisgruppe roles: {}",
//                publishedSkoleRoles.stream()
//                        .map(Role::getRoleId)
//                        .toList()
//        );
//
//        List<String> basisgrupperToPublish = Arrays.asList("ALL");
//
//        List<Role> validBasisgruppeRoles = basisgruppeService.getAllValid(currentTime)
//                .stream()
//                .filter(basisgruppeResource -> basisgrupperToPublish
//                        .contains(basisgruppeResource.getSystemId().getIdentifikatorverdi())
//                        || basisgrupperToPublish.contains("ALL")
//                )
//                .filter(basisgruppeResource -> !basisgruppeResource.getElevforhold().isEmpty())
//                .map(basisgruppeResource -> createOptionalBasisgruppeRole(basisgruppeResource))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//
//        List< Role > publishedBasisgruppeRoles = roleEntityProducerService.publishChangedRoles(validBasisgruppeRoles);
//
//        log.info("Published {} of {} valid basisgruppe roles", publishedBasisgruppeRoles.size(), validBasisgruppeRoles.size());
//        log.debug("Ids of published basisgruppe roles: {}",
//                publishedBasisgruppeRoles.stream()
//                        .map(Role::getRoleId)
//                        .toList()
//        );


//    private Optional<Role> createOptionalSkoleRole(SkoleResource skoleResource, Date currentTime) {
//        Optional<List<Member>> members = Optional.ofNullable(memberService.createSkoleMemberList(skoleResource, currentTime));
//        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(skoleResource);
//        return  Optional.of(
//                    createSkoleRole(skoleResource,
//                    organisasjonselementResource.get(),
//                    members.get())
//        );
//    }
//    private Role createSkoleRole (
//            SkoleResource skoleResource,
//            OrganisasjonselementResource organisasjonselementResource,
//            List<Member> members
//    ) {
//        String roleType = RoleType.ELEV.getRoleType();
//
//        return getEducationRole(
//                organisasjonselementResource,
//                members,
//                roleType,
//                skoleResource.getNavn(),
//                RoleSubType.SKOLEGRUPPE.getRoleSubType(),
//                roleService.createSkoleRoleId(skoleResource, roleType),
//                ResourceLinkUtil.getFirstSelfLink(skoleResource)
//        );
//    }
//    private Optional<Role> createOptionalBasisgruppeRole(BasisgruppeResource basisgruppeResource) {
//        Optional<List<Member>> members = Optional.ofNullable(memberService.createBasisgruppeMemberList(basisgruppeResource));
//        Optional<SkoleResource> optionalSkole = skoleService.getSkole(basisgruppeResource);
//        SkoleResource skoleResource = optionalSkole.get();
//        Optional<OrganisasjonselementResource> organisasjonselementResource = organisasjonselementService.getOrganisasjonsResource(skoleResource);
//
//        return  Optional.of(
//                    createBasisgruppeRole(basisgruppeResource,
//                    organisasjonselementResource.get(),
//                    members.get())
//        );
//    }
//    private Role createBasisgruppeRole(
//            BasisgruppeResource basisgruppeResource,
//            OrganisasjonselementResource organisasjonselementResource,
//            List<Member> members
//    ) {
//        String roleType = RoleType.ELEV.getRoleType();
//
//        return getEducationRole(
//                organisasjonselementResource,
//                members,
//                roleType,
//                basisgruppeResource.getNavn(),
//                RoleSubType.BASISGRUPPE.getRoleSubType(),
//                roleService.createBasisgruppeRoleId(basisgruppeResource, roleType),
//                ResourceLinkUtil.getFirstSelfLink(basisgruppeResource)
//        );
//    }

//    private Role getEducationRole(
//            OrganisasjonselementResource organisasjonselementResource,
//            List<Member> members,
//            String roleType,
//            String groupName,
//            String subRoleType,
//            String roleId,
//            String selfLink
//    ) {
//        String organizationUnitId = organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi();
//        String organizationUnitName = organisasjonselementResource.getNavn();
//
//        return Role
//                .builder()
//                .roleId(roleId)
//                .resourceId(selfLink)
//                .roleName(roleService.createRoleName(groupName, roleType, subRoleType))
//                .roleSource(RoleSource.FINT.getRoleSource())
//                .roleType(roleType)
//                .roleSubType(subRoleType)
//                .aggregatedRole(false)
//                .organisationUnitId(organizationUnitId)
//                .organisationUnitName(organizationUnitName)
//                .members(members)
//                .build();
//    }

}
