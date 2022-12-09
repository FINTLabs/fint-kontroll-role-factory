package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoleEntityProducerService {
    private final EntityProducer<Role> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public RoleEntityProducerService( EntityProducerFactory entityProducerFactory){

        entityProducer = entityProducerFactory.createProducer(Role.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("entityToRole")
                .build();
    }

    public void publish(Role role) {
        String key = role.getResourceId();
        log.info("ResourceId : " + key);
        entityProducer.send(
                EntityProducerRecord.<Role>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(role)
                        .build()
        );
    }
}
