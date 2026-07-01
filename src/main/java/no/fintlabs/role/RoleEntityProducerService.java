package no.fintlabs.role;

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
public class RoleEntityProducerService {
    private final FintCache<String, Role> roleCache;
    private final ParameterizedTemplate<Role> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public RoleEntityProducerService(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            EntityTopicService entityTopicService,
            FintCache<String, Role> roleCache
    ){
        this.roleCache = roleCache;
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(Role.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("role")
                .build();
        entityTopicService.createOrModifyTopic(entityTopicNameParameters,EntityTopicConfiguration
                .stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );
    }

    public List<Role> publishChangedRoles(List<Role> roles) {
        return roles
                .stream()
                .filter(role -> roleCache
                        .getOptional(role.getRoleId())
                        .map(publishedRole -> !role.equals(publishedRole))
                        .orElse(true)
                )
                .peek(role -> log.info("Publish role {} with no members. ResourceId: {}", role.getRoleId(), role.getResourceId()))
                .peek(this::publishChangedRole)
                .toList();
    }

    private void publishChangedRole(Role role) {
        String key = role.getRoleId();
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<Role>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(role)
                        .build()
        );
    }
}
