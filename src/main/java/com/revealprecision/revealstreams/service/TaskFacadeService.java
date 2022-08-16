package com.revealprecision.revealstreams.service;


import com.revealprecision.revealstreams.constants.KafkaConstants;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.dto.TaskFacade;
import com.revealprecision.revealstreams.factory.TaskFacadeFactory;
import com.revealprecision.revealstreams.messaging.message.TaskAggregate;
import com.revealprecision.revealstreams.messaging.message.TaskEvent;
import com.revealprecision.revealstreams.messaging.message.TaskLocationPair;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.props.KafkaProperties;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskFacadeService {


  private final PlanService planService;

  private final LocationService locationService;

  private final StreamsBuilderFactoryBean getKafkaStreams;
  private final KafkaProperties kafkaProperties;


  public List<TaskFacade> syncTasks(List<String> planIdentifiers,
      List<UUID> jurisdictionIdentifiers, Long serverVersion, String requester) {

    List<Plan> plans = planIdentifiers.stream().map(UUID::fromString)
        .map(planService::findPlanByIdentifier).collect(Collectors.toList());

    Map<UUID, List<Location>> planTargetsMap = plans.stream().map(
            plan -> getUuidListSimpleEntry(jurisdictionIdentifiers, plan))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    KafkaStreams kafkaStreams = getKafkaStreams.getKafkaStreams();
    ReadOnlyKeyValueStore<String, TaskEvent> taskPlanParent = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.taskPlanParent),
            QueryableStoreTypes.keyValueStore()));
    ReadOnlyKeyValueStore<String, TaskAggregate> taskParent = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(
            kafkaProperties.getStoreMap().get(KafkaConstants.taskParent),
            QueryableStoreTypes.keyValueStore()));

    ReadOnlyKeyValueStore<String, TaskEvent> taskStore = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType(kafkaProperties.getStoreMap().get(KafkaConstants.task),
            QueryableStoreTypes.keyValueStore()));

    log.debug("Before task sync");

    return plans.stream().flatMap(plan -> {
      if (plan.getPlanTargetType().getGeographicLevel().getName()
          .equals(LocationConstants.STRUCTURE)) {
        return getTaskFacadeStream(serverVersion, requester, planTargetsMap, taskPlanParent,
            taskParent,
            plan);
      } else {
        return getTaskFacadeStream(serverVersion, requester, planTargetsMap, taskStore, plan);
      }
    }).distinct().collect(Collectors.toList());
  }

  private Stream<TaskFacade> getTaskFacadeStream(Long serverVersion, String requester,
      Map<UUID, List<Location>> planTargetsMap, ReadOnlyKeyValueStore<String, TaskEvent> taskStore,
      Plan plan) {
    return planTargetsMap.get(plan.getIdentifier()).stream()
        .map(location -> plan.getIdentifier() + "_" + location.getIdentifier().toString())
        .map(taskStore::get)
        .filter(Objects::nonNull)
        .filter(taskEvent -> taskEvent.getServerVersion() > serverVersion)
        .map(taskEvent -> TaskFacadeFactory.getTaskFacadeObj(requester,
            taskEvent.getParentLocation().toString()
            , taskEvent))
        .collect(Collectors.toList()).stream();
  }

  private Stream<TaskFacade> getTaskFacadeStream(Long serverVersion, String requester,
      Map<UUID, List<Location>> planTargetsMap,
      ReadOnlyKeyValueStore<String, TaskEvent> taskPlanParent,
      ReadOnlyKeyValueStore<String, TaskAggregate> taskParent, Plan plan) {
    return planTargetsMap.get(plan.getIdentifier()).stream()
        .peek(planObj -> log.debug("plan Id for task sync: {}", planObj))
        .flatMap(jurisdictionIdentifier -> {
          String taskKey = plan.getIdentifier() + "_" + jurisdictionIdentifier.getIdentifier();
          log.debug("key to retrieve task: {}", taskKey);
          List<TaskLocationPair> taskIds = new ArrayList<>();
          try {
            taskIds = taskParent.get(taskKey).getTaskIds();
          } catch (NullPointerException exp) {
            log.error("key: {} requested is not present in kafka store", taskKey);
          }
          return taskIds.stream()
              .peek(taskIdList -> log.debug("items retrieved from kafka store: {}", taskIdList))
              .filter(taskId -> taskId.getServerVersion() > serverVersion)
              .map(
                  taskId -> taskId.getId() + "_" + plan.getIdentifier() + "_"
                      + jurisdictionIdentifier.getIdentifier()).map(taskPlanParentId -> {
                TaskEvent task = taskPlanParent.get(taskPlanParentId);
                String locationParent = taskPlanParentId.split("_")[2];
                return TaskFacadeFactory.getTaskFacadeObj(requester, locationParent,
                    task);
              });
        });
  }

  private SimpleEntry<UUID, List<Location>> getUuidListSimpleEntry(
      List<UUID> jurisdictionIdentifiers, Plan plan) {
    List<Location> collect = jurisdictionIdentifiers.stream()
        .map(locationService::findByIdentifier)
        .filter(location -> {
          if (plan.getPlanTargetType().getGeographicLevel().getName()
              .equals(LocationConstants.STRUCTURE)) {
            return
                location.getGeographicLevel().getName().equals(
                    plan.getLocationHierarchy().getNodeOrder().get(
                        plan.getLocationHierarchy().getNodeOrder()
                            .indexOf(LocationConstants.STRUCTURE) - 1));
          } else {
            return location.getGeographicLevel().getName()
                .equals(plan.getPlanTargetType().getGeographicLevel().getName());
          }
        }).collect(Collectors.toList());
    return new SimpleEntry<>(plan.getIdentifier(),
        collect);
  }





}