package com.revealprecision.revealstreams.api.querying;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.revealprecision.revealstreams.constants.KafkaConstants;
import com.revealprecision.revealstreams.messaging.message.LocationFormDataAggregateEvent;
import com.revealprecision.revealstreams.messaging.message.LocationFormDataCountAggregateEvent;
import com.revealprecision.revealstreams.messaging.message.LocationFormDataMinMaxAggregateEvent;
import com.revealprecision.revealstreams.messaging.message.LocationFormDataSumAggregateEvent;
import com.revealprecision.revealstreams.messaging.message.LocationPersonBusinessStateAggregate;
import com.revealprecision.revealstreams.messaging.message.LocationPersonBusinessStateCountAggregate;
import com.revealprecision.revealstreams.messaging.message.LocationStructureBusinessStatusAggregate;
import com.revealprecision.revealstreams.messaging.message.PersonBusinessStatusAggregate;
import com.revealprecision.revealstreams.messaging.message.UserAggregate;
import com.revealprecision.revealstreams.messaging.message.UserParentChildren;
import com.revealprecision.revealstreams.props.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reveal-streams/state-store")
@Slf4j
@RequiredArgsConstructor
@Profile("Reveal-Streams")

public class KafkaStateStoreQueryController<T> {

  private final StreamsBuilderFactoryBean getKafkaStreams;
  private final KafkaProperties kafkaProperties;
  private final ObjectMapper objectMapper;


  @GetMapping("/userParentChildren")
  public void userPerformance() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, UserParentChildren> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap()
                .get(KafkaConstants.userParentChildren),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }


  @GetMapping("/userPerformanceSums")
  public void userPerformanceSums() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, UserAggregate> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap()
                .get(KafkaConstants.userPerformanceSums),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/cddSupervisorLocationFormDataIntegerSumOrAverage")
  public void cddSupervisorLocationFormDataIntegerSumOrAverage() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataCountAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap()
                .get(KafkaConstants.cddSupervisorLocationFormDataIntegerSumOrAverage),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/supervisorLocationFormDataIntegerSumOrAverage")
  public void supervisorLocationFormDataIntegerSumOrAverage() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataCountAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap()
                .get(KafkaConstants.supervisorLocationFormDataIntegerSumOrAverage),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/cddNames")
  public void cddNames() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataCountAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.cddNames),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/mdaLiteSupervisors")
  public void mdaLiteSupervisors() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataCountAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.mdaLiteSupervisors),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/locationFormDataStringCount")
  public void locationFormDataStringCount() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataCountAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.locationFormDataStringCount),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/locationFormDataIntegerSumOrAverage")
  public void locationFormDataIntegerSumOrAverage() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataSumAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.locationFormDataIntegerSumOrAverage),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/locationFormDataMinMax")
  public void locationFormDataMinMax() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataMinMaxAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.locationFormDataMinMax),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

//  @GetMapping("/personFormDataStringCount")
//  public void personFormDataStringCount() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, PersonFormDataCountAggregateEvent> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.personFormDataStringCount),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }

  @GetMapping("/locationFormDataInteger")
  public void locationFormDataInteger() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationFormDataAggregateEvent> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.locationFormDataInteger),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/structurePeople")
  public void structurePeople() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationPersonBusinessStateAggregate> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.structurePeople),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }


  @GetMapping("/locationStructureBusinessStatus")
  public void locationStructureBusinessStatus() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationStructureBusinessStatusAggregate> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.locationStructureBusinessStatus),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/locationStructureHierarchyBusinessStatus")
  public void locationStructureHierarchyBusinessStatus() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationStructureBusinessStatusAggregate> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap()
                .get(KafkaConstants.locationStructureHierarchyBusinessStatus),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

  @GetMapping("/structurePeopleCounts")
  public void structurePeopleCounts() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, LocationPersonBusinessStateCountAggregate> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.structurePeopleCounts),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }


//  @GetMapping("/assignedOperationalCountPerParent")
//  public void assignedOperationalCountPerParent() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.assignedOperationalCountPerParent),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }

  @GetMapping("/personBusinessStatus")
  public void personBusinessStatus() {
    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, PersonBusinessStatusAggregate> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.personBusinessStatus),
            QueryableStoreTypes.keyValueStore())
    );
    iterateThroughStore(counts);
  }

//  @GetMapping("/tableOfOperationalAreas")
//  public void tableOfOperationalAreas() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, OperationalAreaAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.tableOfOperationalAreas),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }

//  @GetMapping("/structureCountPerParent")
//  public void structureCountPerParent() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.structureCountPerParent),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/hierarchicalPeopleTreatmentData")
//  public void test() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, LocationPersonBusinessStateCountAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.hierarchicalPeopleTreatmentData),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/hierarchicalPeopleTreatmentCounts")
//  public void hierarchicalPeopleTreatmentCounts() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, LocationPersonBusinessStateCountAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.hierarchicalPeopleTreatmentCounts),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/operationalAreaTreatmentData")
//  public void operationalAreaTreatmentData() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, LocationPersonBusinessStateCountAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.operationalAreaTreatmentData),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/restructuredOperationalAreaTreatmentData")
//  public void restructuredOperationalAreaTreatmentData() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, LocationPersonBusinessStateCountAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(kafkaProperties.getStoreMap()
//                .get(KafkaConstants.restructuredOperationalAreaTreatmentData),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/tableOfOperationalAreaHierarchiesForPersonStream")
//  public void tableOfOperationalAreaHierarchiesForPersonStream() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, OperationalAreaAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(kafkaProperties.getStoreMap()
//                .get(KafkaConstants.tableOfOperationalAreaHierarchiesForPersonStream),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/joinedOperationalAreaTreatmentData")
//  public void joinedOperationalAreaTreatmentData() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, TreatedOperationalAreaAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.joinedOperationalAreaTreatmentData),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/operationalTreatedCounts")
//  public void operationalTreatedCounts() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, TreatedOperationalAreaAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.operationalTreatedCounts),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }

//  @GetMapping("/assignedStructureCountPerParent")
//  public void countOfAssignedStructure() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.assignedStructureCountPerParent),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }

//  @GetMapping("/tableOfOperationalAreaHierarchies")
//  public void tableOfOperationalAreaHierarchies() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, OperationalAreaAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.tableOfOperationalAreaHierarchies),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/locationBusinessStatusByPlanParentHierarchy")
//  public void locationBusinessStatusByPlanParentHierarchy() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(kafkaProperties.getStoreMap()
//                .get(KafkaConstants.locationBusinessStatusByPlanParentHierarchy),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//  @GetMapping("/operationalAreaByPlanParentHierarchy")
//  public void operationalAreaByPlanParentHierarchy() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, OperationalAreaVisitedCount> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(kafkaProperties.getStoreMap()
//                .get(KafkaConstants.operationalAreaByPlanParentHierarchy),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }
//
//
//  @GetMapping("/locationBusinessStatus")
//  public void locationBusinessStatus() {
//    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
//    ReadOnlyKeyValueStore<String, LocationBusinessStatusAggregate> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType(
//            kafkaProperties.getStoreMap().get(KafkaConstants.locationBusinessStatus),
//            QueryableStoreTypes.keyValueStore())
//    );
//    iterateThroughStore(counts);
//  }











  private void iterateThroughStore(ReadOnlyKeyValueStore<String, ?> counts) {
    KeyValueIterator<String, ?> all = counts.all();
    log.info("Started");
    while (all.hasNext()) {
      KeyValue<String, ?> keyValue = all.next();
      String key = keyValue.key;
      Object value = keyValue.value;
      log.info("key: {} - value: {}", key, objectMapper.valueToTree(value));
    }
    log.info("Ended");
  }

}
