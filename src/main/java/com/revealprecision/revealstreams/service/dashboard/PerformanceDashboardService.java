package com.revealprecision.revealstreams.service.dashboard;


import com.revealprecision.revealstreams.constants.KafkaConstants;
import com.revealprecision.revealstreams.enums.PlanInterventionTypeEnum;
import com.revealprecision.revealstreams.messaging.message.UserLevel;
import com.revealprecision.revealstreams.messaging.message.UserParentChildren;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Organization;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.domain.PlanAssignment;
import com.revealprecision.revealstreams.props.KafkaProperties;
import com.revealprecision.revealstreams.service.OrganizationService;
import com.revealprecision.revealstreams.service.PlanAssignmentService;
import com.revealprecision.revealstreams.service.PlanService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PerformanceDashboardService {


  private final PlanService planService;

  private final PlanAssignmentService planAssignmentService;

  private final OrganizationService organizationService;

  private final IrsPerformanceDashboardService irsPerformanceDashboardService;
  private final IrsLitePerformanceDashboardService irsLitePerformanceDashboardService;

  private ReadOnlyKeyValueStore<String, UserParentChildren> userParentChildren;

  private final StreamsBuilderFactoryBean getKafkaStreams;
  private final KafkaProperties kafkaProperties;
  boolean datastoresInitialized = false;

  public static final String DAYS_WORKED = "Number of Days Worked";
  public static final String AVERAGE_DAYS_WORKED = "Average Days Worked";
  public static final String START_TIME = "Data Collection Start Time";
  public static final String END_TIME = "Data Collection End Time";
  public static final String AVERAGE_START_TIME = "Average Start Time";
  public static final String AVERAGE_END_TIME = "Average End Time";
  public static final String HOURS_WORKED = "Duration in Field";
  public static final String AVERAGE_HOURS_WORKED = "Average Duration in Field Per Day";

  public List<RowData> getDataForReport(UUID planIdentifier, String id) {

    Plan plan = planService.findPlanByIdentifier(planIdentifier);
    if (plan.getInterventionType().getCode().equals(PlanInterventionTypeEnum.IRS_LITE.name())) {
      Map<String, UserLevel> userLevels = null;
      if (id == null || id.equals("null") || id.equals("")) {
        List<PlanAssignment> planAssignmentsByPlanIdentifier = planAssignmentService.getPlanAssignmentsByPlanIdentifier(
            planIdentifier);
        userLevels = planAssignmentsByPlanIdentifier.stream()
            .map(this::getOrganization)
            .filter(Objects::nonNull)
            .map(highestLevelOrg -> new UserLevel(highestLevelOrg.getIdentifier().toString(),
                highestLevelOrg.getName(), 0, "organization", highestLevelOrg.getType().name()))
            .collect(Collectors.toMap(UserLevel::getUserId, userLevel -> userLevel, (a, b) -> b));
      } else {
        UserParentChildren userParentChildren = this.userParentChildren.get(
            planIdentifier + "_" + id);
        if (userParentChildren != null) {
          userLevels = userParentChildren.getChildren().stream()
              .collect(Collectors.toMap(UserLevel::getUserId, userLevel -> userLevel, (a, b) -> b));
        }
      }
      initialDataStores(plan);
      return getRowDatasIRSLite(plan,
          userLevels == null ? null : new HashSet<>(userLevels.values()), id);

    } else {
      boolean startAtTop = false;
      if (id == null || id.equals("null") || id.equals("")) {
        startAtTop = true;
      }
      return getRowDatasIRS(plan, id, startAtTop);
    }

  }


  private Organization getOrganization(PlanAssignment planAssignment) {
    Organization loopOrg = organizationService.findByIdWithChildren(
        planAssignment.getOrganization().getIdentifier());
    Organization highestLevelOrg = null;
    while (loopOrg != null) {
      if (loopOrg.getParent() == null) {
        highestLevelOrg = loopOrg;
      }
      loopOrg = loopOrg.getParent();
    }

    return highestLevelOrg;
  }

  public List<RowData> getDatedRowDatas(UUID planIdentifier, String id) {

    Plan plan = planService.findPlanByIdentifier(planIdentifier);

    initialDataStores(plan);

    switch (PlanInterventionTypeEnum.valueOf(plan.getInterventionType().getCode())) {
      case IRS:
        return irsPerformanceDashboardService.getDetailedPerformanceColumnData(
            plan, id);
      case IRS_LITE:
        return irsLitePerformanceDashboardService.getDetailedPerformanceColumnData(plan, id);
      case MDA:
      case MDA_LITE:
      default:
        return null;
    }
  }


  private List<RowData> getPerformanceColumnDataIRS(
      Plan plan,
      String parentUserLevelId, boolean startAtTop) {

    return irsPerformanceDashboardService.getRowData(
        plan, parentUserLevelId, startAtTop);


  }

  private RowData getPerformanceColumnDataIRSLite(
      Plan plan, UserLevel userLevel,
      String parentUserLevelId) {

    return irsLitePerformanceDashboardService.getRowData(
        plan, userLevel, parentUserLevelId);

  }

  private List<RowData> getRowDatasIRSLite(Plan plan, Set<UserLevel> userLevels,
      String parentUserLevelId) {


    if (userLevels == null) {
      return null;
    }
    return userLevels.stream()
        .map(userLevel -> getPerformanceColumnDataIRSLite(
            plan, userLevel, parentUserLevelId)).collect(
            Collectors.toList());
  }

  private List<RowData> getRowDatasIRS(Plan plan,
      String parentUserLevelId, boolean startAtTop) {
    return getPerformanceColumnDataIRS(plan,  parentUserLevelId, startAtTop);
  }



  private void initialDataStores(Plan plan) {
    if (!datastoresInitialized) {
      this.userParentChildren = getKafkaStreams.getKafkaStreams().store(
          StoreQueryParameters.fromNameAndType(kafkaProperties.getStoreMap().get(
                  KafkaConstants.userParentChildren),
              QueryableStoreTypes.keyValueStore()));
      datastoresInitialized = true;
    }
    switch (PlanInterventionTypeEnum.valueOf(plan.getInterventionType().getCode())) {
      case IRS:
        break;
      case IRS_LITE:
        irsLitePerformanceDashboardService.initialDataStores();
        break;
      case MDA:
      case MDA_LITE:
      default:
    }


  }


}
