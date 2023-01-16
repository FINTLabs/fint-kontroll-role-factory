package no.fintlabs.personalressurs;

import no.fintlabs.cache.FintCache;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonalressursService {
    private final FintCache<String, PersonalressursResource> personalressursCache;

    public PersonalressursService(FintCache<String, PersonalressursResource> personalressursCache) {
        this.personalressursCache = personalressursCache;
    }
    public Optional<PersonalressursResource> getPersonalressurs(String personalressursHref){
        return personalressursCache.getOptional(personalressursHref);
    }
}



