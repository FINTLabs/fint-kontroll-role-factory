package no.fintlabs.role;

import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.member.MemberUtils;
import no.fintlabs.termin.TerminService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UndervisningsgruppeService {

    private final ElevforholdService elevforholdService;
    private final UndervisningsgruppemedlemskapService undervisningsgruppemedlemskapService;
    private final GyldighetsperiodeService gyldighetsperiodeService;
    private  final TerminService terminService;
    private final FintCache<String, UndervisningsgruppeResource> undervisningsgruppeResourceCache;
    private final FintCache<String, TerminResource> terminResourceCache;


    public UndervisningsgruppeService(
            ElevforholdService elevforholdService, UndervisningsgruppemedlemskapService undervisningsgruppemedlemskapService, GyldighetsperiodeService gyldighetsperiodeService,
            TerminService terminService,
            FintCache<String, UndervisningsgruppeResource> undervisningsgruppeResourceCache,
            FintCache<String, TerminResource> terminResourceCache
            ) {
        this.elevforholdService = elevforholdService;
        this.undervisningsgruppemedlemskapService = undervisningsgruppemedlemskapService;
        this.gyldighetsperiodeService = gyldighetsperiodeService;
        this.terminService = terminService;
        this.undervisningsgruppeResourceCache = undervisningsgruppeResourceCache;
        this.terminResourceCache = terminResourceCache;
    }

    public List<UndervisningsgruppeResource> getAllValid(Date currentTime) {
        return undervisningsgruppeResourceCache.getAllDistinct()
                .stream()
                // TODO Add termin testdata
                //.filter(undervisningsgruppeResource -> terminService.hasValidPeriod(undervisningsgruppeResource.getTermin(), currentTime))
                .toList();
    }
    public List<ElevforholdResource> getValidAllElevforhold(
            UndervisningsgruppeResource undervisningsgruppeResource,
            Date currentTime
            ) {
        return undervisningsgruppeResource.getGruppemedlemskap()
                .stream()
                .map(undervisningsgruppemedlemskapService::getUndervisningsgruppemedlemskap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(undervisningsgruppemedlemskapResource ->
                        undervisningsgruppemedlemskapResource.getGyldighetsperiode()==null
                                || MemberUtils.getUndervisningsgruppemedlemskapStatus(undervisningsgruppemedlemskapResource,currentTime).equals("ACTIVE"))
                .map(elevforholdService::getElevforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}