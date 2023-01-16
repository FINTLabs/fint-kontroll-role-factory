package no.fintlabs.role;

import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.kodeverk.TerminResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import  no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import  no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import  no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class ResourceEntityCacheConfiguration {

    private final FintCacheManager fintCacheManager;

    public ResourceEntityCacheConfiguration(FintCacheManager fintCacheManager) {
        this.fintCacheManager = fintCacheManager;
    }
/*
    @Bean
    FintCache<String, ElevResource> elevResourceCache() {
        return createCache(ElevResource.class);
    }

    @Bean
    FintCache<String, SkoleressursResource> skoleressursResourceCache() {
        return createCache(SkoleressursResource.class);
    }

    @Bean
    FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceCache() {
        return createCache(UndervisningsforholdResource.class);
    }
*/
    @Bean
    FintCache<String, PersonalressursResource> personalressursResourceCache() {
        return createCache(PersonalressursResource.class);
    }

    @Bean
    FintCache<String, PersonResource> personResourceCache() {
        return createCache(PersonResource.class);
    }

    @Bean
    FintCache<String, OrganisasjonselementResource> organisasjonselementResourceCache() {
        return createCache(OrganisasjonselementResource.class);
    }

    @Bean
    FintCache<String, ArbeidsforholdResource> arbeidsforholdResourceCache() {
        return createCache(ArbeidsforholdResource.class);
    }
    @Bean
    FintCache<String, BasisgruppeResource> basisgruppeResourceCache() {
        return createCache(BasisgruppeResource.class);
    }

    @Bean
    FintCache<String, TerminResource> terminResourceCache() {
        return createCache(TerminResource.class);
    }
    @Bean
    FintCache<String, BasisgruppemedlemskapResource> basisgruppemedlemskapResourceCache() {
        return createCache(BasisgruppemedlemskapResource.class);
    }
    @Bean
    FintCache<String, ElevforholdResource> elevforholdResourceCache() {
        return createCache(ElevforholdResource.class);
    }
    @Bean
    FintCache<String , SkoleResource> skoleResourceCache()
    {
        return createCache(SkoleResource.class);
    }
    @Bean
    FintCache<String, Integer> publishedEntityHashCache() {
        return createCache(Integer.class);
    }
    @Bean
    FintCache<String, Long> memberCache() {
        return createCache(Long.class);
    }

    private <V> FintCache<String, V> createCache(Class<V> resourceClass) {
        return fintCacheManager.createCache(
                resourceClass.getName().toLowerCase(Locale.ROOT),
                String.class,
                resourceClass
        );
    }
}
