package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.termin.TerminService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UndervisningsgruppeService {

    private final ElevforholdService elevforholdService;
    private final UndervisningsgruppemedlemskapService undervisningsgruppemedlemskapService;
    private final GyldighetsperiodeService gyldighetsperiodeService;
    private final FintCache<String, UndervisningsgruppeResource> undervisningsgruppeResourceCache;

    public List<UndervisningsgruppeResource> getAllValid() {
        return undervisningsgruppeResourceCache.getAllDistinct()
                .stream()
                .toList();
    }

    public List<ElevforholdResource> getValidAllElevforhold(
            UndervisningsgruppeResource undervisningsgruppeResource, Date currentTime) {
        List<ElevforholdResource> gruppemedlemskap =  undervisningsgruppeResource.getGruppemedlemskap()
                .stream()
                .map(undervisningsgruppemedlemskapService::getUndervisningsgruppemedlemskap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(undervisningsgruppemedlemskapResource ->
                        undervisningsgruppemedlemskapResource.getGyldighetsperiode()==null || gyldighetsperiodeService.isValid(undervisningsgruppemedlemskapResource.getGyldighetsperiode(), currentTime))
                .map(elevforholdService::getElevforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Link> selfLinks = undervisningsgruppeResource.getSelfLinks();
        log.info("Found {} gruppemedlemskap for undervisningsgruppe {} with resourceid {}", gruppemedlemskap.size(), undervisningsgruppeResource.getNavn(),
                (selfLinks.isEmpty() ? "no self link" : selfLinks.getFirst().getHref())
        );
        return gruppemedlemskap;

    }
    public List<UndervisningsgruppemedlemskapResource> getAllGruppemedlemskap(
            UndervisningsgruppeResource undervisningsgruppeResource) {
        return undervisningsgruppeResource.getGruppemedlemskap()
                .stream()
                .map(undervisningsgruppemedlemskapService::getUndervisningsgruppemedlemskap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
