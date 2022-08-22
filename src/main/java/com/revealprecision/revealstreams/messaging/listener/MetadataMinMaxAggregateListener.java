package com.revealprecision.revealstreams.messaging.listener;

import com.revealprecision.revealstreams.constants.EntityTagDataTypes;
import com.revealprecision.revealstreams.constants.KafkaConstants;
import com.revealprecision.revealstreams.factory.EntityTagEventFactory;
import com.revealprecision.revealstreams.messaging.message.EntityTagEvent;
import com.revealprecision.revealstreams.messaging.message.LocationFormDataMinMaxAggregateEvent;
import com.revealprecision.revealstreams.persistence.domain.EntityTag;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.props.KafkaProperties;
import com.revealprecision.revealstreams.service.EntityTagService;
import com.revealprecision.revealstreams.service.LocationService;
import com.revealprecision.revealstreams.service.MetadataService;
import com.revealprecision.revealstreams.service.PlanService;
import com.revealprecision.revealstreams.util.UserUtils;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("Reveal-Streams")
public class MetadataMinMaxAggregateListener extends Listener {

  ReadOnlyKeyValueStore<String, LocationFormDataMinMaxAggregateEvent> locationFormDataMinMax;
  private final MetadataService metadataService;
  private final EntityTagService entityTagService;
  private final PlanService planService;
  private final LocationService locationService;
  private final StreamsBuilderFactoryBean getKafkaStreams;
  private final KafkaProperties kafkaProperties;


  @KafkaListener(topics = "#{kafkaConfigProperties.topicMap.get('METADATA_MINMAX_AGGREGATE')}", groupId = "reveal_server_group")
  public void listenGroupFoo(ConsumerRecord<String, LocationFormDataMinMaxAggregateEvent> message) {
    init();
    locationFormDataMinMax = getKafkaStreams.getKafkaStreams().store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.locationFormDataMinMax),
            QueryableStoreTypes.keyValueStore()));
    log.info("Received Message k: {}  v: {}", message.key(), message.value());
    String k = message.key();
    LocationFormDataMinMaxAggregateEvent aggMessage = message.value();

    String[] keySplit = k.split("_");

    String planId = keySplit[0];
    Plan plan = null;
    if (planId!=null && !planId.equals("plan") ){
      plan = planService.findNullablePlanByIdentifier(UUID.fromString(planId));
    }

    String entityParentIdentifier = keySplit[2];

    Location location = locationService.findByIdentifier(
        UUID.fromString(entityParentIdentifier));

    UUID entityTagIdentifier = aggMessage.getEntityTagIdentifier();
    LocationFormDataMinMaxAggregateEvent locationFormDataMinMaxAggregateEvent = locationFormDataMinMax.get(
        k);

    if (locationFormDataMinMaxAggregateEvent != null) {

      Optional<EntityTag> entityTagById = entityTagService.findEntityTagById(entityTagIdentifier);

      if (entityTagById.isPresent()) {
        EntityTag entityTag = entityTagById.get();

        if (locationFormDataMinMaxAggregateEvent.getMin() != null) {

          Optional<EntityTag> entityTagByTagName = entityTagService.getEntityTagByTagName(
              entityTag.getTag() + "-min");

          if (entityTagByTagName.isPresent()) {
            EntityTagEvent entityTagEvent = EntityTagEventFactory.getEntityTagEvent(entityTagByTagName.get());
            entityTagEvent.setValueType(EntityTagDataTypes.DOUBLE);
            if (entityTagEvent.isAggregate()) {
              metadataService.updateMetaData(UUID.fromString(entityParentIdentifier), locationFormDataMinMaxAggregateEvent.getMin(),
                  plan, null,
                  UserUtils.getCurrentPrincipleName(), entityTagEvent.getValueType(), entityTagEvent,
                  "aggregate", location, "aggregate",
                  Location.class, k + "-min", null);
            }
          }
        }
        if (locationFormDataMinMaxAggregateEvent.getMax() != null) {

          Optional<EntityTag> entityTagByTagName = entityTagService.getEntityTagByTagName(
              entityTag.getTag() + "-max");

          if (entityTagByTagName.isPresent()) {
            EntityTagEvent entityTagEvent = EntityTagEventFactory.getEntityTagEvent(entityTagByTagName.get());
            entityTagEvent.setValueType(EntityTagDataTypes.DOUBLE);
            if (entityTagEvent.isAggregate()) {
              metadataService.updateMetaData(UUID.fromString(entityParentIdentifier), locationFormDataMinMaxAggregateEvent.getMax(),
                  plan, null,
                  UserUtils.getCurrentPrincipleName(), entityTagEvent.getValueType(), entityTagEvent,
                  "aggregate", location, "aggregate",
                  Location.class, k + "-max", null);
            }
          }
        }
      }
    }


  }
}
