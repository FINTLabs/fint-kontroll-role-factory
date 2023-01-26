package no.fintlabs.member;

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fintlabs.arbeidsforhold.ArbeidsforholdService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.organisasjonselement.OrganisasjonselementService;
import no.fintlabs.role.RolePublishingComponent;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    private final FintCache<String , Member> memberCache;
    private final OrganisasjonselementService organisasjonselementService;
    private final ArbeidsforholdService arbeidsforholdService;

    public MemberService(
            FintCache<String, Member> memberCache,
            OrganisasjonselementService organisasjonselementService,
            ArbeidsforholdService arbeidsforholdService
    ) {
        this.memberCache = memberCache;
        this.organisasjonselementService = organisasjonselementService;
        this.arbeidsforholdService = arbeidsforholdService;
    }

    public List<Member> createOrgUnitMemberList (
            OrganisasjonselementResource organisasjonselementResource,
            Date currentTime
    ) {
        return organisasjonselementService.getAllValidArbeidsforhold(organisasjonselementResource, currentTime)
                .stream()
                .map(arbeidsforholdResource -> arbeidsforholdService.getPersonalressurs(arbeidsforholdResource))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(personalressursResource -> personalressursResource.getAnsattnummer().getIdentifikatorverdi())
                .map(href->href.substring(href.lastIndexOf("/") + 1))
                .map(employeeNumber -> getMember(employeeNumber))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Member> getMember (String memberId)
    {
        return memberCache.getOptional(memberId);
    }
}
