package no.fintlabs.role;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.membership.Membership;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
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

    @KafkaListener(topics = "utdanning.elev.skoleressurs")
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
    @KafkaListener(topics = "utdanning.timeplan.undervisningsgruppemedlemskap")
    @Bean
    ConcurrentMessageListenerContainer<String, UndervisningsgruppemedlemskapResource> undervisningsgruppemedlemskapResourceEntityConsumer(
                FintCache<String, UndervisningsgruppemedlemskapResource> undervisningsgruppemedlemskapResourceCache
        ) {
        return createCacheConsumer(
                "utdanning.timeplan.undervisningsgruppemedlemskap",
                UndervisningsgruppemedlemskapResource.class,
                undervisningsgruppemedlemskapResourceCache
        );
    }
    @KafkaListener(topics = "utdanning.elev.elev")
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
    @KafkaListener(topics = "utdanning.elev.elevforhold")
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
    @KafkaListener(topics = "utdanning.timeplan.undervisningsgruppe")
    @Bean
    ConcurrentMessageListenerContainer<String, UndervisningsgruppeResource> undervisningsgruppeResourceEntityConsumer(
            FintCache<String, UndervisningsgruppeResource> undervisningsgruppeResourceCache
    ) {
        return createCacheConsumer(
                "utdanning.timeplan.undervisningsgruppe",
                UndervisningsgruppeResource.class,
                undervisningsgruppeResourceCache
        );
    }
    @KafkaListener(topics = "utdanning.utdanningsprogram.skole")
    @Bean
    ConcurrentMessageListenerContainer<String, SkoleResource> skoleResourceEntityConsumer(
            FintCache<String, SkoleResource> skoleResourceCache
    ) {
        return createCacheConsumer(
                "utdanning.utdanningsprogram.skole",
                SkoleResource.class,
                skoleResourceCache
        );
    }

    @KafkaListener(topics = "administrasjon.organisasjon.organisasjonselement")
    @Bean
    ConcurrentMessageListenerContainer<String, OrganisasjonselementResource> organisasjonselementResourceEntityConsumer(
            FintCache<String , OrganisasjonselementResource> organisasjonselementResourceCache
    ) {
        return  createCacheConsumer(
                "administrasjon.organisasjon.organisasjonselement",
                OrganisasjonselementResource.class,
                organisasjonselementResourceCache
        );
    }
    @KafkaListener(topics = "administrasjon.personal.arbeidsforhold")
    @Bean
    ConcurrentMessageListenerContainer<String, ArbeidsforholdResource> arbeidsforholdResourceEntityConsumer(
            FintCache<String , ArbeidsforholdResource> arbeidsforholdResourceCache
    ) {
        return  createCacheConsumer(
                "administrasjon.personal.arbeidsforhold",
                ArbeidsforholdResource.class,
                arbeidsforholdResourceCache
        );
    }
    @KafkaListener(topics = "administrasjon.personal.personalressurs")
    @Bean
    ConcurrentMessageListenerContainer<String, PersonalressursResource> personalressursResourceEntityConsumer(
            FintCache<String , PersonalressursResource>personalressursResourceCache
    ) {
        return  createCacheConsumer(
                "administrasjon.personal.personalressurs",
                PersonalressursResource.class,
                personalressursResourceCache
        );
    }
    @KafkaListener(topics = "role")
    @Bean
    ConcurrentMessageListenerContainer<String, Role> roleEntityConsumer(
            FintCache<String, Role> roleCache
    ) {
        return entityConsumerFactoryService.createFactory(
                Role.class,
                consumerRecord -> {
                    Role role = consumerRecord.value();
                    roleCache.put(
                            role.getRoleId(),
                            role
                    );
                }
        ).createContainer(EntityTopicNameParameters.builder().resource("role").build());
    }
    @KafkaListener(topics = "role-membership")
    @Bean
    ConcurrentMessageListenerContainer<String, Membership> membershipEntityConsumer(
            FintCache<String, Membership> membershipCache
    ) {
        return entityConsumerFactoryService.createFactory(
                Membership.class,
                consumerRecord -> {
                    String key = consumerRecord.key();
                    Membership membership = consumerRecord.value();
                    membershipCache.put(
                            key,
                            membership
                    );
                }
        ).createContainer(EntityTopicNameParameters.builder().resource("role-membership").build());
    }
    @KafkaListener(topics = "role-catalog-role")
    @Bean
    ConcurrentMessageListenerContainer<String, RoleCatalogRole> roleCatalogRoleEntityConsumer(
            FintCache<String, RoleCatalogRole> roleCatalogCache
    ) {
        return entityConsumerFactoryService.createFactory(
                RoleCatalogRole.class,
                consumerRecord -> {
                    RoleCatalogRole roleCatalogRole = consumerRecord.value();
                    roleCatalogCache.put(
                            roleCatalogRole.getRoleId(),
                            roleCatalogRole
                    );
                }
        ).createContainer(EntityTopicNameParameters.builder().resource("role-catalog-role").build());
    }
}
