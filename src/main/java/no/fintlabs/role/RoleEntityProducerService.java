package no.fintlabs.role;

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
public class RoleEntityProducerService {
    private final FintCache<String, Integer> publishedRoleHashCache;
    private final EntityProducer<Role> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public RoleEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService,
            FintCache<String, Integer> publishedRoleHashCache){
        entityProducer = entityProducerFactory.createProducer(Role.class);
        this.publishedRoleHashCache = publishedRoleHashCache;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }

    public List<Role> publishChangedRoles(List<Role> roles) {
        return roles
                .stream()
                .filter(role -> publishedRoleHashCache
                        .getOptional(role.getRoleId())
                        .map(publishedRoleHash -> publishedRoleHash != role.hashCode())
                        .orElse(true)
                )
                .peek(this::publishChangedRole)
                .toList();
    }

    private void publishChangedRole(Role role) {
        String key = role.getRoleId();
        entityProducer.send(
                EntityProducerRecord.<Role>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(role)
                        .build()
        );
    }
}
