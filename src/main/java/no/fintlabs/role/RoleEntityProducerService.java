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
    private final FintCache<String, Role> roleCache;
    private final EntityProducer<Role> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public RoleEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService,
            FintCache<String, Role> roleCache
    ){
        this.roleCache = roleCache;
        entityProducer = entityProducerFactory.createProducer(Role.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }

    public List<Role> publishChangedRoles(List<Role> roles) {
        return roles
                .stream()
//                .filter(role -> roleCache
//                        .getOptional(role.getRoleId())
//                        .map(publishedRole -> !role.equals(publishedRole))
//                        .orElse(true)
//                )
                .peek(role -> log.info("Publish role {} with {} members"
                , role.getRoleId()
                , role.getMembers().size()))
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
