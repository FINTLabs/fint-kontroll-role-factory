package no.fintlabs.membership;

import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class MembershipEntityProducerService {
    private final FintCache<String, Membership> membershipCache;
    private final EntityProducer<Membership> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
            ;

    public MembershipEntityProducerService(
        FintCache<String, Membership> membershipCache,
        EntityProducerFactory entityProducerFactory,
        EntityTopicService entityTopicService
    ){
        this.membershipCache = membershipCache;
        entityProducer = entityProducerFactory.createProducer(Membership.class);
        entityTopicNameParameters = EntityTopicNameParameters
            .builder()
            .resource("role-membership")
            .build();
    entityTopicService.ensureTopic(entityTopicNameParameters, 0);
}

    public List<Membership> publishChangedMemberships(List<Membership> memberships) {
        return memberships
            .stream()
            .filter(membership -> membershipCache
                .getOptional(getMembershipKey(membership))
                .map(publishedMembership -> !membership.equals(publishedMembership))
                .orElse(true)
            )
            .peek(membership -> log.info("Publish membership {} with status {}"
                , getMembershipKey(membership)
                , membership.getMemberStatus()
            ))
            .peek(this::publishChangedMembership)
            .toList();
    }
    @AsyncPublisher(
            operation = @AsyncOperation(
                    channelName = "role-membership",
                    description = "Publish role membership"
            )
    )
    @KafkaAsyncOperationBinding
    private void publishChangedMembership(Membership membership) {
        String key = getMembershipKey(membership);
        entityProducer.send(
            EntityProducerRecord.<Membership>builder()
                .topicNameParameters(entityTopicNameParameters)
                .key(key)
                .value(membership)
                .build()
        );
    }
    private String getMembershipKey(Membership membership) {
        return membership.getRoleId().toString() +"_"+ membership.getMemberId().toString();
    }
}
