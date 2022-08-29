package com.revealprecision.revealstreams.messaging.streams;


import com.revealprecision.revealstreams.constants.KafkaConstants;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.messaging.message.LocationAssigned;
import com.revealprecision.revealstreams.messaging.message.PlanLocationAssignMessage;
import com.revealprecision.revealstreams.messaging.serdes.RevealSerdes;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.LocationRelationship;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.props.KafkaProperties;
import com.revealprecision.revealstreams.service.LocationRelationshipService;
import com.revealprecision.revealstreams.service.LocationService;
import com.revealprecision.revealstreams.service.PlanService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Repartitioned;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class LocationStream {

  private final LocationRelationshipService locationRelationshipService;
  private final LocationService locationService;
  private final KafkaProperties kafkaProperties;
  private final PlanService planService;
  private final Logger streamLog = LoggerFactory.getLogger("stream-file");
  private final RevealSerdes revealSerdes;

  @Bean
  KStream<String, PlanLocationAssignMessage> propogateLocationAssignment(
      StreamsBuilder streamsBuilder) {

    KStream<String, PlanLocationAssignMessage> locationsAssignedStream = streamsBuilder.stream(
        kafkaProperties.getTopicMap().get(KafkaConstants.PLAN_LOCATION_ASSIGNED),
        Consumed.with(Serdes.String(), revealSerdes.get(PlanLocationAssignMessage.class)));

    //Doing this so that we can rewind this topic without rewinding to cause task generation from running again....
    locationsAssignedStream.to(
        kafkaProperties.getTopicMap().get(KafkaConstants.PLAN_LOCATION_ASSIGNED_STREAM));
    return locationsAssignedStream;
  }

  @Bean
  KStream<String, PlanLocationAssignMessage> getAssignedStructures(
      StreamsBuilder streamsBuilder) {

    // getting values from plan assignment
    KStream<String, PlanLocationAssignMessage> locationsAssignedStream = streamsBuilder.stream(
        kafkaProperties.getTopicMap().get(KafkaConstants.PLAN_LOCATION_ASSIGNED_STREAM),
        Consumed.with(Serdes.String(), revealSerdes.get(PlanLocationAssignMessage.class)));

    locationsAssignedStream.peek(
        (k, v) -> streamLog.debug("locationsAssignedStream - k: {} v: {}", k, v));

    //Get structures from the locations assigned to plan
    KStream<String, PlanLocationAssignMessage> stringPlanLocationAssignMessageKStream = locationsAssignedStream
        .mapValues((k, planLocationAssignMessage) -> {
          planLocationAssignMessage.setLocationsRemoved(
              planLocationAssignMessage.getLocationsRemoved().stream()
                  .filter(locationRemoved -> locationService.findByIdentifier(
                          UUID.fromString(locationRemoved)).getGeographicLevel().getName()
                      .equals(LocationConstants.OPERATIONAL)).collect(
                      Collectors.toList()));
          planLocationAssignMessage.setLocationsAdded(
              planLocationAssignMessage.getLocationsAdded().stream()
                  .filter(locationAdded -> locationService.findByIdentifier(
                          UUID.fromString(locationAdded)).getGeographicLevel().getName()
                      .equals(LocationConstants.OPERATIONAL)).collect(
                      Collectors.toList()));
          return planLocationAssignMessage;
        });

    stringPlanLocationAssignMessageKStream.peek(
        (k, v) -> streamLog.debug("stringPlanLocationAssignMessageKStream - k: {} v: {}", k, v));

    KStream<String, LocationAssigned> stringLocationAssignedKStream = stringPlanLocationAssignMessageKStream
        .flatMapValues(
            (k, planLocationAssignMessage) -> getStructuresAssignedAndUnAssigned(
                planLocationAssignMessage))
        .flatMapValues(
            (k, locationAssigned) -> getLocationAssignedUnpackedByAncestry(locationAssigned))
        .selectKey((key, locationAssigned) -> getLocationPlanAncestorKey(locationAssigned))
        .mapValues(
            (key, locationAssigned) -> locationAssigned.isAssigned() ? locationAssigned : null);

    stringLocationAssignedKStream.peek(
        (k, v) -> streamLog.debug("stringLocationAssignedKStream - k: {} v: {}", k, v));

    KTable<String, LocationAssigned> tableOfAssignedStructures = stringLocationAssignedKStream
        .repartition(Repartitioned.with(Serdes.String(),new JsonSerde<>(LocationAssigned.class)))
        .toTable(Materialized.<String, LocationAssigned, KeyValueStore<Bytes, byte[]>>as(
                kafkaProperties.getStoreMap()
                    .get(KafkaConstants.tableOfAssignedStructuresWithParentKeyed))
            .withKeySerde(Serdes.String())
            .withValueSerde(new JsonSerde<>(LocationAssigned.class)));

    tableOfAssignedStructures.toStream().peek(
        (k, v) -> streamLog.debug("tableOfAssignedStructures.toStream() - k: {} v: {}", k, v));

    KTable<String, Long> count = tableOfAssignedStructures
        .groupBy((key, locationAssigned) -> KeyValue.pair(
                locationAssigned.getPlanIdentifier() + "_" + locationAssigned.getAncestor(),
                locationAssigned),
            Grouped.with(Serdes.String(), new JsonSerde<>(LocationAssigned.class)))
        .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(
                kafkaProperties.getStoreMap().get(KafkaConstants.assignedStructureCountPerParent))
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.Long()));

    count.toStream().peek((k, v) -> streamLog.debug("count.toStream() - k: {} v: {}", k, v));
    return locationsAssignedStream;
  }


  private String getLocationPlanAncestorKey(LocationAssigned locationAssigned) {
    return locationAssigned.getIdentifier() + "_"
        + locationAssigned.getPlanIdentifier() + "_" + locationAssigned.getAncestor();
  }

  private List<LocationAssigned> getLocationAssignedUnpackedByAncestry(
      LocationAssigned locationAssigned) {
    return locationAssigned.getAncestry().stream().map(ancestor -> {
      LocationAssigned locationAssigned1 = new LocationAssigned();
      locationAssigned1.setAssigned(locationAssigned.isAssigned());
      locationAssigned1.setIdentifier(locationAssigned.getIdentifier());
      locationAssigned1.setPlanIdentifier(locationAssigned.getPlanIdentifier());
      locationAssigned1.setAncestor(ancestor);
      return locationAssigned1;
    }).collect(Collectors.toList());
  }


  private List<LocationAssigned> getStructuresAssignedAndUnAssigned(
      PlanLocationAssignMessage planLocationAssignMessage) {

    List<LocationAssigned> locations = new ArrayList<>();
    String planIdentifier = planLocationAssignMessage.getPlanIdentifier().toString();

    Plan plan = planService.findPlanByIdentifier(UUID.fromString(planIdentifier));

    if (planLocationAssignMessage.getLocationsAdded() != null) {

      List<LocationAssigned> locationsAssigned = planLocationAssignMessage.getLocationsAdded()
          .stream().flatMap(locationAdded -> {
            List<Location> locations1 =
                locationRelationshipService.getChildrenLocations(
                    plan.getLocationHierarchy().getIdentifier(),
                    UUID.fromString(locationAdded));
            return locations1.stream();
          }).map(Location::getIdentifier)
          .map(locationIdentifier -> {
            LocationRelationship locationRelationshipsForLocation = locationRelationshipService.getLocationRelationshipsForLocation(
                plan.getLocationHierarchy().getIdentifier(), locationIdentifier);

            LocationAssigned locationAssigned = new LocationAssigned();
            locationAssigned.setAssigned(true);
            locationAssigned.setAncestry(locationRelationshipsForLocation.getAncestry());
            locationAssigned.setPlanIdentifier(planIdentifier);
            locationAssigned.setIdentifier(locationIdentifier.toString());

            return locationAssigned;
          })
          .collect(Collectors.toList());
      locations.addAll(locationsAssigned);
    }

    if (planLocationAssignMessage.getLocationsRemoved() != null) {
      List<LocationAssigned> locationsUnAssigned = planLocationAssignMessage.getLocationsRemoved()
          .stream().flatMap(locationRemoved -> {
            List<Location> locations1 =
                locationRelationshipService.getChildrenLocations(
                    plan.getLocationHierarchy().getIdentifier(),
                    UUID.fromString(locationRemoved));
            return locations1.stream();
          }).map(Location::getIdentifier)
          .map(locationIdentifier -> {
            LocationRelationship locationRelationshipsForLocation = locationRelationshipService.getLocationRelationshipsForLocation(
                plan.getLocationHierarchy().getIdentifier(), locationIdentifier);

            LocationAssigned locationAssigned = new LocationAssigned();
            locationAssigned.setAssigned(false);
            locationAssigned.setAncestry(locationRelationshipsForLocation.getAncestry());
            locationAssigned.setPlanIdentifier(planIdentifier);
            locationAssigned.setIdentifier(locationIdentifier.toString());

            return locationAssigned;
          })
          .collect(Collectors.toList());
      locations.addAll(locationsUnAssigned);
    }
    return locations;
  }
}
