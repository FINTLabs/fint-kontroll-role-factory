package no.fintlabs.role;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import no.fintlabs.cache.FintCache;
import no.fintlabs.user.User;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import no.fintlabs.links.ResourceLinkUtil;
import no.fintlabs.membership.Membership;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Objects;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService;
    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;


    public ResourceEntityConsumersConfiguration(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults
    ) {
        this.parameterizedListenerContainerFactoryService = parameterizedListenerContainerFactoryService;
        this.kafkaConsumerConfigurationDefaults = kafkaConsumerConfigurationDefaults;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass,
            FintCache<String, T> cache
    ) {
        return createRecordListenerFactory(
                resourceClass,
                consumerRecord -> cache.put(
                        ResourceLinkUtil.getSelfLinks(consumerRecord.value()),
                        consumerRecord.value()
                )
        ).createContainer(topic(resourceReference));
    }

    private <T> ParameterizedListenerContainerFactory<T> createRecordListenerFactory(
            Class<T> resourceClass,
            java.util.function.Consumer<ConsumerRecord<String, T>> recordProcessor
    ) {
        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                resourceClass,
                recordProcessor,
                kafkaConsumerConfigurationDefaults.seekToBeginningListenerConfiguration(),
                kafkaConsumerConfigurationDefaults.defaultErrorHandler()
        );
    }

    private EntityTopicNameParameters topic(String resourceName) {
        return kafkaConsumerConfigurationDefaults.defaultEntityTopic(resourceName);
    }


    @Bean
    ConcurrentMessageListenerContainer<String, SkoleressursResource> skoleressursResourceEntityConsumer(
            FintCache<String, SkoleressursResource> skoleressursResourceCache
    ) {
        return createCacheConsumer(
                "utdanning-elev-skoleressurs",
                SkoleressursResource.class,
                skoleressursResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, UndervisningsgruppemedlemskapResource> undervisningsgruppemedlemskapResourceEntityConsumer(
            FintCache<String, UndervisningsgruppemedlemskapResource> undervisningsgruppemedlemskapResourceCache
    ) {
        return createCacheConsumer(
                "utdanning-timeplan-undervisningsgruppemedlemskap",
                UndervisningsgruppemedlemskapResource.class,
                undervisningsgruppemedlemskapResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ElevResource> elevResourceEntityConsumer(
            FintCache<String, ElevResource> elevResourceCache
    ) {
        return createCacheConsumer(
                "utdanning-elev-elev",
                ElevResource.class,
                elevResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ElevforholdResource> elevforholdResourceEntityConsumer(
            FintCache<String, ElevforholdResource> elevforholdResourceResourceCache
    ) {
        return createCacheConsumer(
                "utdanning-elev-elevforhold",
                ElevforholdResource.class,
                elevforholdResourceResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, UndervisningsgruppeResource> undervisningsgruppeResourceEntityConsumer(
            FintCache<String, UndervisningsgruppeResource> undervisningsgruppeResourceCache
    ) {
        return createCacheConsumer(
                "utdanning-timeplan-undervisningsgruppe",
                UndervisningsgruppeResource.class,
                undervisningsgruppeResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, SkoleResource> skoleResourceEntityConsumer(
            FintCache<String, SkoleResource> skoleResourceCache
    ) {
        return createCacheConsumer(
                "utdanning-utdanningsprogram-skole",
                SkoleResource.class,
                skoleResourceCache
        );
    }


    @Bean
    ConcurrentMessageListenerContainer<String, OrganisasjonselementResource> organisasjonselementResourceEntityConsumer(
            FintCache<String, OrganisasjonselementResource> organisasjonselementResourceCache
    ) {
        return createCacheConsumer(
                "administrasjon-organisasjon-organisasjonselement",
                OrganisasjonselementResource.class,
                organisasjonselementResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ArbeidsforholdResource> arbeidsforholdResourceEntityConsumer(
            FintCache<String, ArbeidsforholdResource> arbeidsforholdResourceCache
    ) {
        return createCacheConsumer(
                "administrasjon-personal-arbeidsforhold",
                ArbeidsforholdResource.class,
                arbeidsforholdResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, PersonalressursResource> personalressursResourceEntityConsumer(
            FintCache<String, PersonalressursResource> personalressursResourceCache
    ) {
        return createCacheConsumer(
                "administrasjon-personal-personalressurs",
                PersonalressursResource.class,
                personalressursResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, Role> roleEntityConsumer(
            FintCache<String, Role> roleCache
    ) {
        return createRecordListenerFactory(
                Role.class,
                consumerRecord -> {
                    Role role = consumerRecord.value();
                    roleCache.put(
                            role.getRoleId(),
                            role
                    );
                }
        ).createContainer(topic("role"));
    }

    @Bean
    ConcurrentMessageListenerContainer<String, Membership> membershipEntityConsumer(
            FintCache<String, Membership> membershipCache
    ) {
        return createRecordListenerFactory(
                Membership.class,
                consumerRecord -> {
                    String key = consumerRecord.key();
                    Membership membership = consumerRecord.value();
                    membershipCache.put(
                            key,
                            membership
                    );
                }
        ).createContainer(topic("role-membership"));
    }

    @Bean
    ConcurrentMessageListenerContainer<String, RoleCatalogRole> roleCatalogRoleEntityConsumer(
            FintCache<String, RoleCatalogRole> roleCatalogCache
    ) {
        return createRecordListenerFactory(
                RoleCatalogRole.class,
                consumerRecord -> {
                    RoleCatalogRole roleCatalogRole = consumerRecord.value();
                    roleCatalogCache.put(
                            roleCatalogRole.getRoleId(),
                            roleCatalogRole
                    );
                }
        ).createContainer(topic("role-catalog-role"));
    }

    @Bean
    ConcurrentMessageListenerContainer<String, User> userConsumer(
            FintCache<String, User> userCache
    ) {
        return createRecordListenerFactory(
                User.class,
                (ConsumerRecord<String, User> consumerRecord)
                        ->
                {
                    if (!Objects.equals(consumerRecord.value().getStatus(), "DELETED"))
                        userCache.put(consumerRecord.value().getResourceId(),
                                consumerRecord.value());
                    else {
                        userCache.remove(consumerRecord.value().getResourceId());
                    }
                }
        ).createContainer(topic("kontrolluser"));
    }


}
