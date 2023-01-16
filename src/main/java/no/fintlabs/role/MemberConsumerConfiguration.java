package no.fintlabs.role;

import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.member.Member;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class MemberConsumerConfiguration {
    @Bean
    public ConcurrentMessageListenerContainer<String, Member> memberConsumer(
            FintCache<String, Long> memberIdCache,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("member")
                .build();

        ConcurrentMessageListenerContainer container = entityConsumerFactoryService.createFactory(
                        Member.class,
                        (ConsumerRecord<String,Member> consumerRecord)
                                -> memberIdCache.put(consumerRecord.value().getResourceId(),
                                        consumerRecord.value().getId()
                                )
        ).createContainer(entityTopicNameParameters);

        return container;
    }
}
