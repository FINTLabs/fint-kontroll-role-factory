package no.fintlabs.user;

import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Objects;

@Configuration
public class UserConsumerConfiguration {
    @Bean
    public ConcurrentMessageListenerContainer<String, User> userConsumer(
            FintCache<String, User> userCache,
            EntityConsumerFactoryService entityConsumerFactoryService
    ) {
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("kontrolluser")
                .build();

        return entityConsumerFactoryService.createFactory(
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
        ).createContainer(entityTopicNameParameters);
    }
}