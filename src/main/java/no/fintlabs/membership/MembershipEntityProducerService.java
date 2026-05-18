package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.producing.ParameterizedProducerRecord;
import no.novari.kafka.producing.ParameterizedTemplate;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import no.novari.kafka.topic.EntityTopicService;
import no.novari.kafka.topic.configuration.EntityCleanupFrequency;
import no.novari.kafka.topic.configuration.EntityTopicConfiguration;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Service
@Slf4j
public class MembershipEntityProducerService {
    private final FintCache<String, Membership> membershipCache;
    private final ParameterizedTemplate<Membership> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public MembershipEntityProducerService(
            FintCache<String, Membership> membershipCache,
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            EntityTopicService entityTopicService
    ) {
        this.membershipCache = membershipCache;
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(Membership.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("role-membership")
                .build();
        entityTopicService.createOrModifyTopic(entityTopicNameParameters,EntityTopicConfiguration.stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );

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

    private void publishChangedMembership(Membership membership) {
        String key = getMembershipKey(membership);
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<Membership>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(membership)
                        .build()
        );
    }

    private String getMembershipKey(Membership membership) {
        return membership.getRoleId().toString() + "_" + membership.getMemberId().toString();
    }
}
