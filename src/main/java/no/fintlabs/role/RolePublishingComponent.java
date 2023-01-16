package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class RolePublishingComponent {
    /*
    private final FintCache<String, ElevResource> elevResourceCache;
    private final FintCache<String, ElevforholdResource> elevforholdResourceFintCache;
    private final FintCache<String, SkoleressursResource> skoleressursResourceFintCache;
    private final FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache;

    */
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache;
    private final  FintCache<String, TerminResource> terminResourceCache;
    private final FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache;
    private final RoleEntityProducerService roleEntityProducerService;
    private final BasisgruppeService basisgruppeService;
    private final BasisgruppemedlemskapService basisgruppemedlemskapService;
    private final ElevforholdService elevforholdService;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;
    private  final SimpleMemberService simpleMemberService;

    //private final SkoleService skoleService;

    public RolePublishingComponent(
            /*
            FintCache<String, ElevResource> elevResourceCache,
            FintCache<String, ElevforholdResource> elevforholdResourceFintCache,
            FintCache<String, SkoleressursResource> skoleressursResourceFintCache,
            FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache,

            SkoleService skoleService,
             */
            FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache,
            FintCache<String, TerminResource> terminResourceCache,
            FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache,
            RoleEntityProducerService roleEntityProducerService,
            BasisgruppeService basisgruppeService,
            BasisgruppemedlemskapService basisgruppemedlemskapService,
            ElevforholdService elevforholdService,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService,
            SimpleMemberService simpleMemberService) {
        /*
        this.elevResourceCache = elevResourceCache;
        this.elevforholdResourceFintCache = elevforholdResourceFintCache;
        this.skoleressursResourceFintCache = skoleressursResourceFintCache;
        this.undervisningsforholdResourceFintCache = undervisningsforholdResourceFintCache;
         */
        this.basisgruppeResourceFintCache = basisgruppeResourceFintCache;
        this.terminResourceCache = terminResourceCache;
        this.basisgruppemedlemskapResourceFintCache = basisgruppemedlemskapResourceFintCache;
        this.roleEntityProducerService = roleEntityProducerService;
        //this.skoleService = skoleService;
        this.basisgruppeService = basisgruppeService;
        this.basisgruppemedlemskapService = basisgruppemedlemskapService;
        this.elevforholdService = elevforholdService;

        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
        this.simpleMemberService = simpleMemberService;

    }

    @Scheduled(
            //initialDelay = 20000L,
            //fixedDelay = 20000L
            initialDelayString = "${fint.kontroll.role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.publishing.fixed-delay}"
    )
        public void publishRoles() {
        Date currentTime = Date.from(Instant.now());

        List<String> basisgrupperToPublish = Arrays.asList("");

        List<Role> validRoles = basisgruppeService.getAllValid(currentTime)
                .stream()
                .filter(basisgruppeResource -> basisgrupperToPublish.contains(basisgruppeResource.getSystemId().getIdentifikatorverdi()))
                .filter(basisgruppeResource -> !basisgruppeResource.getElevforhold().isEmpty())
                .map(basisgruppeResource -> createOptionalRole(basisgruppeResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

                List< Role > publishedRoles = roleEntityProducerService.publishChangedRoles(validRoles);

        log.info("Published {} of {} valid roles", publishedRoles.size(), validRoles.size());
        log.debug("Ids of published roles: {}",
                publishedRoles.stream()
                        .map(Role::getResourceId)
                        .map(href -> href.substring(href.lastIndexOf("/") + 1))
                        .toList()
        );

        List <String> organisasjonselementToPublish = Arrays.asList("38","48");

        List<Role> validOrgUnitRoles = organisasjonselementService.getAllValid(currentTime)
                .stream()
                .filter(organisasjonselementResource -> organisasjonselementToPublish.contains(organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi()))
                .map(organisasjonselementResource -> createOptionalOrgUnitRole(organisasjonselementResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        publishedRoles = roleEntityProducerService.publishChangedRoles(validOrgUnitRoles);

        log.info("Published {} of {} valid org unit roles", publishedRoles.size(), validOrgUnitRoles.size());
        log.debug("Ids of published org unit roles: {}",
                publishedRoles.stream()
                        .map(Role::getResourceId)
                        .map(href -> href.substring(href.lastIndexOf("/") + 1))
                        .toList()
        );
    }

    private Optional<Role> createOptionalRole(BasisgruppeResource basisgruppeResource) {

        return  Optional.of(
                    createRole(basisgruppeResource)
        );
    }
    private Role createRole(
            BasisgruppeResource basisgruppeResource
    ) {
        String groupName = basisgruppeResource.getNavn();
        String roleType = RoleType.ELEV.getRoleType();
        String subRoleType = RoleSubType.BASISGRUPPE.getRoleSubType();
        List<SimpleMember> members = createMemberList(basisgruppeResource);

        return Role
                .builder()
                //.id(Long.valueOf(basisgruppeResource.getSystemId().getIdentifikatorverdi()))
                .resourceId(ResourceLinkUtil.getFirstSelfLink(basisgruppeResource))
                .roleName(createRoleName(groupName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                .members(members)
                .build();
    }
    private Optional<Role> createOptionalOrgUnitRole(OrganisasjonselementResource organisasjonselementResource, Date currentTime) {

        return  Optional.of(
                createOrgUnitRole(organisasjonselementResource, currentTime)
        );
    }
    private Role createOrgUnitRole(
            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime) {
        String groupName = organisasjonselementResource.getNavn();
        String roleType = RoleType.ANSATT.getRoleType();
        String subRoleType = RoleSubType.ORGANISASJONSELEMENT.getRoleSubType();
        List<SimpleMember> members = createOrgUnitMemberList(organisasjonselementResource, currentTime);

        return Role
                .builder()
                //.id(Long.valueOf(organisasjonselementResource.getSystemId().getIdentifikatorverdi()))
                .resourceId(ResourceLinkUtil.getFirstSelfLink(organisasjonselementResource))
                .roleName(createRoleName(groupName, roleType, subRoleType))
                .roleSource(RoleSource.FINT.getRoleSource())
                .roleType(roleType)
                .roleSubType(roleType)
                .members(members)
                .build();
    }
    //TODO: TO be moved to Role class?
    private String createRoleName (String groupName, String roleType, String subRoleType)
    {
        return StringUtils.capitalize(roleType + " i " + subRoleType) + " " + groupName;
    }
    private List<SimpleMember> createOrgUnitMemberList (OrganisasjonselementResource organisasjonselementResource, Date currentTime)
    {
        return organisasjonselementService.getAllValidArbeidsforhold(organisasjonselementResource, currentTime)
                .stream()
                .map(arbeidsforholdResource -> arbeidsforholdService.getPersonalressurs(arbeidsforholdResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(personalressursResource -> personalressursResource.getAnsattnummer().getIdentifikatorverdi())
                .map(href->href.substring(href.lastIndexOf("/") + 1))
                .map(employeeNumber -> simpleMemberService.getSimpleMember(employeeNumber))
                .map(Optional::get)
                .toList();
    }
    private List<SimpleMember> createMemberList (BasisgruppeResource basisgruppeResource)
    {
        return basisgruppeService.getGruppemedlemskapHrefs(basisgruppeResource)
                .stream()
                .map(memberHref -> basisgruppemedlemskapService.getElevforholdHref(memberHref).get())
                .map(elevforholdHref -> elevforholdService.getElevHref(elevforholdHref).get())
                .map(href->href.substring(href.lastIndexOf("/") + 1))
                .map(Long::parseLong)
                .map(SimpleMember::new)
                .toList();
    }
}
