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
import no.fintlabs.member.Member;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.UUID;

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
    @Bean
    FintCache<String, BasisgruppeResource> basisgruppeResourceCache() {
        return createResourceCache(BasisgruppeResource.class);
    }

    @Bean
    FintCache<String, TerminResource> terminResourceCache() {
        return createResourceCache(TerminResource.class);
    }
    @Bean
    FintCache<String, BasisgruppemedlemskapResource> basisgruppemedlemskapResourceCache() {
        return createResourceCache(BasisgruppemedlemskapResource.class);
    }
    @Bean
    FintCache<String, ElevforholdResource> elevforholdResourceCache() {
        return createResourceCache(ElevforholdResource.class);
    }
    @Bean
    FintCache<String, ElevResource> elevResourceCache() {
        return createResourceCache(ElevResource.class);
    }
    @Bean
    FintCache<String , SkoleResource> skoleResourceCache()
    {
        return createResourceCache(SkoleResource.class);
    }
*/
    @Bean
    FintCache<String, PersonalressursResource> personalressursResourceCache() {
        return createResourceCache(PersonalressursResource.class);
    }

    @Bean
    FintCache<String, PersonResource> personResourceCache() {
        return createResourceCache(PersonResource.class);
    }

    @Bean
    FintCache<String, OrganisasjonselementResource> organisasjonselementResourceCache() {
        return createResourceCache(OrganisasjonselementResource.class);
    }

    @Bean
    FintCache<String, ArbeidsforholdResource> arbeidsforholdResourceCache() {
        return createResourceCache(ArbeidsforholdResource.class);
    }
    @Bean
    FintCache<String, Role> roleCache() {return createResourceCache(Role.class); }
    @Bean
    FintCache<String, Member> memberCache() { return createResourceCache(Member.class); }
    @Bean
    FintCache<String , Long> memberIdCache() {return createResourceCache(Long.class); }

    private <V> FintCache<String, V> createResourceCache(Class<V> resourceClass) {
        return fintCacheManager.createCache(
                resourceClass.getName().toLowerCase(Locale.ROOT),
                String.class,
                resourceClass
        );
    }

}
