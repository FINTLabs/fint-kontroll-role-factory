package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResources;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import no.fintlabs.utils.MemberUtils;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class MemberService {
    private final FintCache<String , User> userCache;
    private final UserService userService;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;

    public MemberService(
            FintCache<String, User> userCache, UserService userService, FintCache<String,
            PersonalressursResource> personalressursResourceCache,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService
    ) {
        this.userCache = userCache;
        this.userService = userService;
        this.personalressursResourceCache = personalressursResourceCache;
        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
    }

    public List<Member> createOrgUnitMemberList ( OrganisasjonselementResource organisasjonselementResource, Date currentTime ) {

        log.debug("Creating member list for org unit {} ({})"
                , organisasjonselementResource.getOrganisasjonsId()
                ,organisasjonselementResource.getOrganisasjonsKode());

        return organisasjonselementService.getAllValidArbeidsforhold(organisasjonselementResource, currentTime)
                .stream()
                .map(arbeidsforholdResource -> createMember(arbeidsforholdResource, currentTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(member -> {
                    log.debug("Member {} has status {}", member.getUserName(), member.getMemberStatus());
                } )
                .distinct()
                .toList();
    }

    private List<PersonalressursResource> getManagersThisSubUnit(OrganisasjonselementResource organisasjonselementResource) {
         return organisasjonselementService.getSubOrgUnitsThisOrgUnit(organisasjonselementResource)
                .stream()
                .map(arbeidssted -> ResourceLinkUtil.getOptionalFirstLink(arbeidssted::getLeder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(personalressursResourceCache::getOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }


    private Optional<Member> createMember(ArbeidsforholdResource arbeidsforholdResource, Date currentTime) {

        Optional<PersonalressursResource> personalressursResource = arbeidsforholdService.getPersonalressurs(arbeidsforholdResource);

        if (personalressursResource.isEmpty()) {
            return Optional.empty();
        }

        Optional<User> user = userService.getUser(personalressursResource.get().getAnsattnummer().getIdentifikatorverdi());

        if (user.isEmpty()) {
            return Optional.empty();
        }

        String memberStatus = MemberUtils.getMemberStatus(arbeidsforholdResource, currentTime);
        //Date memberStatusDate = memberStatus.equals("ACTIVE")
//                ? arbeidsforholdResource.getGyldighetsperiode().getStart()
//                : arbeidsforholdResource.getGyldighetsperiode().getSlutt();

        //, memberStatusDate
        return Optional.of(CreateMember(user.get(), memberStatus));
    }

    private Member CreateMember(
            User user,
            String memberStatus
            //,Date memberStatusDate
    ){
        //, memberStatusDate
        return user.toMember(memberStatus);
    }
}
