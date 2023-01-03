package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.user.User;
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
    private final FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache;
    */
    private final FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache;
    private final  FintCache<String, TerminResource> terminResourceCache;
    private final RoleEntityProducerService roleEntityProducerService;
    private final BasisgruppeService basisgruppeService;

    //private final SkoleService skoleService;

    public RolePublishingComponent(
            /*
            FintCache<String, ElevResource> elevResourceCache,
            FintCache<String, ElevforholdResource> elevforholdResourceFintCache,
            FintCache<String, SkoleressursResource> skoleressursResourceFintCache,
            FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceFintCache,
            FintCache<String , BasisgruppemedlemskapResource> basisgruppemedlemskapResourceFintCache,
            SkoleService skoleService,
             */
            FintCache<String, BasisgruppeResource> basisgruppeResourceFintCache,
            FintCache<String, TerminResource> terminResourceCache,
            RoleEntityProducerService roleEntityProducerService,
            BasisgruppeService basisgruppeService) {
        /*
        this.elevResourceCache = elevResourceCache;
        this.elevforholdResourceFintCache = elevforholdResourceFintCache;
        this.skoleressursResourceFintCache = skoleressursResourceFintCache;
        this.undervisningsforholdResourceFintCache = undervisningsforholdResourceFintCache;
        this. basisgruppemedlemskapResourceFintCache = basisgruppemedlemskapResourceFintCache;
         */
        this.basisgruppeResourceFintCache = basisgruppeResourceFintCache;
        this.terminResourceCache = terminResourceCache;
        this.roleEntityProducerService = roleEntityProducerService;
        //this.skoleService = skoleService;
        this.basisgruppeService = basisgruppeService;
    }

    @Scheduled(
            //initialDelay = 20000L,
            //fixedDelay = 20000L
            initialDelayString = "${fint.kontroll.role.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role.publishing.fixed-delay}"
    )
        public void publishRoles() {
        Date currentTime = Date.from(Instant.now());

        List<String> basisgrupperToPublish = Arrays.asList("1468038");

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
    }

    private Optional<Role> createOptionalRole(BasisgruppeResource basisgruppeResource) {
        //
        return  Optional.of(
                createRole(
                        basisgruppeResource
                )
        );
    }

    private Role createRole(
            BasisgruppeResource basisgruppeResource
    ) {
        String groupName = basisgruppeResource.getNavn();
        String roleType = RoleType.ELEV.getRoleType();
        String subRoleType = RoleSubType.BASISGRUPPE.getRoleSubType();
        List<Member> members = createMemberList(basisgruppeResource);

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

    //TODO: TO be moved to Role class?
    private String createRoleName (String groupName, String roleType, String subRoleType)
    {
        return StringUtils.capitalize(roleType + " i " + subRoleType) + " " + groupName;
    }
    private List<Member> createMemberList (BasisgruppeResource basisgruppeResource)
    {
        return basisgruppeResource.getElevforhold()
                .stream()
                .map(elevforhold ->elevforhold.getHref())
                .map(href->href.substring(href.lastIndexOf("/") + 1))
                .map(Long::parseLong)
                .map(Member::new)
                .toList();
    }
}
