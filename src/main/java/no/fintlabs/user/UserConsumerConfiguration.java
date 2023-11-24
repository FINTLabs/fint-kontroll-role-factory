package no.fintlabs.user;

import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.member.Member;
import no.fintlabs.user.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class UserConsumerConfiguration {
    @Bean
    public ConcurrentMessageListenerContainer<String, User> userConsumer(
            FintCache<String, User> userCache,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("kontrolluser")
                .build();

        return entityConsumerFactoryService.createFactory(
                User.class,
                (ConsumerRecord<String,User> consumerRecord)
                        -> userCache.put(consumerRecord.value().getResourceId(),
                        consumerRecord.value()
                )
        ).createContainer(entityTopicNameParameters);
    }
}