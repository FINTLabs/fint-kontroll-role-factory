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
import no.fintlabs.role.*;
import no.fintlabs.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MemberService {
    private final FintCache<String , User> userCache;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;

    public MemberService(
            FintCache<String, User> userCache, FintCache<String,
            PersonalressursResource> personalressursResourceCache,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService
    ) {
        this.userCache = userCache;
        this.personalressursResourceCache = personalressursResourceCache;
        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
    }
    public List<Member> createOrgUnitMemberList (
            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime
    ) {
        String orgUnitCode = organisasjonselementResource.getOrganisasjonsKode().getIdentifikatorverdi();
        String orgUnitId = organisasjonselementResource.getOrganisasjonsId().getIdentifikatorverdi();

        PersonalressursResources resources = new PersonalressursResources();
        log.info("Creating member list for org unit {}({}) with {} users in the user cache"
                , orgUnitId
                , orgUnitCode
                , userCache.getAll().size()
        );
        List<ArbeidsforholdResource> validArbeidsforholdResources = organisasjonselementService.getAllValidArbeidsforhold(organisasjonselementResource, currentTime);
        log.info("Found {} valid arbeidsforhold in org unit {}({})"
                , validArbeidsforholdResources.size()
                , orgUnitId
                ,orgUnitCode
            );
        validArbeidsforholdResources
                .stream()
                .map(arbeidsforholdResource -> arbeidsforholdService.getPersonalressurs(arbeidsforholdResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .forEach(resources::addResource);;

        int noOfNonLeaderEmployees = resources.getSize();
        log.info("Found {} valid non manager personalressurser in org unit {]({})"
                , noOfNonLeaderEmployees
                , orgUnitId
                ,orgUnitCode
        );

        if (!organisasjonselementResource.getUnderordnet().isEmpty()) {
            getManagersThisSubUnit(organisasjonselementResource).forEach(resources::addResource);;
        }
        log.info("Found {} valid manager personalressurser in org unit {}({})"
                , resources.getSize() - noOfNonLeaderEmployees
                , orgUnitId
                ,orgUnitCode
        );
        log.info("Trying to match found personalressurs-resources with users in the user cache");
        List<Member> members = resources.getContent()
                .stream()
                .map(PersonalressursResource::getAnsattnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(href -> getMember(href))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        log.info("Found {} members for org unit {} ({})", members.size()
            , orgUnitId
            , orgUnitCode
        );
        return members;
    }

    private List<PersonalressursResource> getManagersThisSubUnit(OrganisasjonselementResource organisasjonselementResource) {
         return organisasjonselementService.getSubOrgUnitsThisOrgUnit(organisasjonselementResource)
                .stream()
                .map(arbeidssted -> ResourceLinkUtil.getOptionalFirstLink(arbeidssted::getLeder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(leder -> personalressursResourceCache.getOptional(leder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Member> getMember (String userId)
    {
        Optional<User> optionalUser =userCache.getOptional(userId);

        if (!optionalUser.isEmpty()) {
            return Optional.of(optionalUser.get().toMember());
        }
        return Optional.empty();

    }
}
