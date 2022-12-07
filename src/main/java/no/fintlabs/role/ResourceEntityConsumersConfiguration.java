package no.fintlabs.role;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.utdanning.utdanningsprogram.Skole;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final EntityConsumerFactoryService entityConsumerFactoryService;


    public ResourceEntityConsumersConfiguration(EntityConsumerFactoryService entityConsumerFactoryService) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass,
            FintCache<String, T> cache
    ) {
        return entityConsumerFactoryService.createFactory(
                resourceClass,
                consumerRecord -> cache.put(
                        ResourceLinkUtil.getSelfLinks(consumerRecord.value()),
                        consumerRecord.value()
                ),
                new CommonLoggingErrorHandler()
        ).createContainer(EntityTopicNameParameters.builder().resource(resourceReference).build());
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ElevResource> elevResourceEntityConsumer(
            FintCache<String, ElevResource> elevResourceCache
    ) {
        return createCacheConsumer(
                "utdanning.elev.elev",
                ElevResource.class,
                elevResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, SkoleressursResource> skoleressursResourceEntityConsumer(
            FintCache<String, SkoleressursResource> skoleressursResourceCache
    ) {
        return createCacheConsumer(
                "utdanning.elev.skoleressurs",
                SkoleressursResource.class,
                skoleressursResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, UndervisningsforholdResource> undervisningsforholdResourceConsumer(
            FintCache<String, UndervisningsforholdResource> undervisningsforholdResourceCache
    ) {
        return createCacheConsumer(
                "utdanning.elev.undervisningsforhold",
                UndervisningsforholdResource.class,
                undervisningsforholdResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ElevforholdResource> elevforholdResourceEntityConsumer(
            FintCache<String, ElevforholdResource> elevforholdResourceResourceCache
    ) {
        return createCacheConsumer(
                "utdanning.elev.elevforhold",
                ElevforholdResource.class,
                elevforholdResourceResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, BasisgruppeResource> basisgruppeResourceEntityConsumer(
            FintCache<String, BasisgruppeResource> basisgruppeResourceCache
    ) {
        return createCacheConsumer(
                "utdanning.elev.basisgruppe",
                BasisgruppeResource.class,
                basisgruppeResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, BasisgruppemedlemskapResource> basisgruppemedlemskapResourceEntityConsumer(
                FintCache<String, BasisgruppemedlemskapResource> basisgruppemedlemskapResourceCache
        ) {
    return createCacheConsumer(
            "utdanning.elev.basisgruppemedlemskap",
            BasisgruppemedlemskapResource.class,
            basisgruppemedlemskapResourceCache
    );
}

}
