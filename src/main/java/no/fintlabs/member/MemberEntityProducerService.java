package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.role.Role;
import no.fintlabs.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MemberEntityProducerService {
    private final FintCache<String, Integer> publishedMemberHashCache;
    private final EntityProducer<Member> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public MemberEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService,
            FintCache<String, Integer> publishedMemberHashCache){
        entityProducer = entityProducerFactory.createProducer(Member.class);
        this.publishedMemberHashCache = publishedMemberHashCache;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("member")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }

    public List<Member> publishChangedMembers(List<Member> members) {
        return members
                .stream()
                .filter(member -> publishedMemberHashCache
                        .getOptional(member.getResourceId())
                        .map(publishedMemberHash -> publishedMemberHash != member.hashCode())
                        .orElse(true)
                )
                .peek(this::publishChangedMember)
                .toList();
    }

    private void publishChangedMember(Member member) {
        String key = member.getResourceId();
        entityProducer.send(
                EntityProducerRecord.<Member>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(member)
                        .build()
        );
    }
}
