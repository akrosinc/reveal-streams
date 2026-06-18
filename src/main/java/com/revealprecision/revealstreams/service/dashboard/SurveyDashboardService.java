package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus.NOT_ELIGIBLE;
import static com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus.NOT_VISITED;
import static com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus.NO_APPROPRIATE_ADULT_AVAILABLE;
import static com.revealprecision.revealstreams.util.DashboardUtils.getBusinessStatusColor;

import com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.factory.LocationResponseFactory;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.domain.TaskBusinessStateTracker;
import com.revealprecision.revealstreams.persistence.projection.GdrsCountsProjection;
import com.revealprecision.revealstreams.persistence.projection.HdssEventDataProjection;
import com.revealprecision.revealstreams.persistence.projection.IndividualTaskBusinessStateByLocationProjection;
import com.revealprecision.revealstreams.persistence.projection.IndividualsByLocationProjection;
import com.revealprecision.revealstreams.persistence.projection.IndividualsPerCompoundByLocationProjection;
import com.revealprecision.revealstreams.persistence.projection.IndividualsPerCompoundByLocationProjectionObj;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import com.revealprecision.revealstreams.persistence.projection.LocationMultipartCountProjection;
import com.revealprecision.revealstreams.persistence.repository.HdssCompoundsRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationRepository;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.props.InstanceProperties;
import com.revealprecision.revealstreams.service.LocationBusinessStatusService;
import com.revealprecision.revealstreams.service.PlanLocationsService;
import com.revealprecision.revealstreams.util.DashboardUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class SurveyDashboardService {


  public static final String LOCATION = "LOCATION";
  public static final String TOTAL_TESTED = "TotalTested";
  public static final String COMPOUND = "Compound";
  public static final String LOCATION_NAME = "LocationName";
  private final PlanLocationsService planLocationsService;
  private final DashboardProperties dashboardProperties;
  private final LocationBusinessStatusService locationBusinessStatusService;
  private final InstanceProperties instanceProperties;
  private final LocationRepository locationRepository;
  private final HdssCompoundsRepository hdssCompoundsRepository;

  private static final String TOTAL_STRUCTURES = "Total structures";
  private static final String TOTAL_STRUCTURES_TARGETED = "Total Structures Targeted";
  private static final String TOTAL_STRUCTURES_VISITED = "Total structures visited";
  private static final String TOTAL_STRUCTURES_MDA_COMPLETE_OR_PARTIALLY_COMPLETE = "Total Structures MDA Complete or Partially complete MDA";
  private static final String STRUCTURE_STATUS = "Structure Status";
  public static final String VISITATION_COVERAGE = "Visitation Coverage (Visited/Target)";
  public static final String COMPLETION_COVERAGE = "Completion Coverage (Visited/Target)";
  public static final String DISTRIBUTION_COVERAGE = "Distribution Coverage (MDA Completed/Visited)";

  public static final String COMPLETE = "Complete";
  public static final String MONTH_THREE_COMPLETE_COUNT = "Month Three Complete";
  public static final String MONTH_SIX_COMPLETE_COUNT = "Month Six Complete";
  public static final String ENROLLED_COUNT = "Enrolled";
  public static final String NOT_ENROLLED_COUNT = "Not Enrolled";
  public static final String ENROLLED_NOT_COMPLETE = "Enrolled Not Complete";
  public static final String CLUSTER_COUNT = "Cluster Count";

  public static final String TOTAL_INDIVIDUALS = "Total # HDSS Individuals";
  public static final String TOTAL_INDEX_CASES = "Total # Passive HF index cases";
  public static final String TOTAL_INDIVIDUALS_TESTED = "Total # Individuals tested for RACD";
  public static final String TOTAL_RCD_CASES = "Total # RACD cases";
  public static final String TOTAL_CASES = "Total # Cases (Passive HF Index + RCD)";
  public static final String PASSIVE_CASE_PERCENTAGE = "Passive index case detection ratio";
  public static final String RACD_BASED_MALARIA_PREVALENCE = "RACD-based malaria prevalence %";

  public static final String INDEX_CASE_MEMBER = "Index Case Member";
  public static final String SECONDARY_INDEX_CASE_MEMBER = "Secondary Index Case Member";

  public List<RowData> getIRSFullData(Plan plan, Location childLocation) {

    Map<String, ColumnData> columns = new LinkedHashMap<>();

    Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap =
        locationBusinessStatusService.getLocationBusinessStateObjPerGeoLevel(
            plan.getIdentifier(), childLocation.getIdentifier(),
            childLocation.getGeographicLevel().getName(),
            plan.getLocationHierarchy().getIdentifier());

    Long totalStructuresCountObj = locationBusinessStatusService.getLocationCountsForGeoLevelByHierarchyLocationParent(
        childLocation.getIdentifier(), plan.getLocationHierarchy().getIdentifier(),
        LocationConstants.STRUCTURE, plan);

    long totalStructuresTargetedCountObj = locationBusinessStatusService.getTotalLocationsByParentAndPlan(
        plan.getIdentifier(),
        childLocation.getIdentifier());

    log.debug("child location: {} - {}, totalStructuresCountObj: {}",
        childLocation.getIdentifier(), childLocation.getName(), totalStructuresCountObj);

    log.debug("child location: {} - {}, locationBusinessStateObjPerGeoLevelMap: {}",
        childLocation.getIdentifier(), childLocation.getName(),
        locationBusinessStateObjPerGeoLevelMap);

    log.debug("child location: {} - {}, totalStructuresTargetedCountObj: {}",
        childLocation.getIdentifier(), childLocation.getName(), totalStructuresTargetedCountObj);

    if (instanceProperties.getClient().equals("uw")) {

      columns.put(TOTAL_STRUCTURES,
          getTotalStructuresCounts(totalStructuresCountObj,
              locationBusinessStateObjPerGeoLevelMap));

      columns.put(TOTAL_STRUCTURES_TARGETED,
          getTotalStructuresTargetedCount(totalStructuresTargetedCountObj,
              locationBusinessStateObjPerGeoLevelMap));
      columns.put(
          TOTAL_STRUCTURES_VISITED,
          getTotalStructuresFoundCount(totalStructuresTargetedCountObj,
              locationBusinessStateObjPerGeoLevelMap));

      LocationMultipartCountProjection clusterCount = locationRepository.getMultipartLocationCountByLocationParent(
          childLocation.getIdentifier(), plan.getLocationHierarchy().getIdentifier(), "cluster");

      ColumnData totalEnrolledStructuresByState = getTotalEnrolledStructuresByState(
          locationBusinessStateObjPerGeoLevelMap);

      ColumnData totalMonthThreeStructuresByState = getTotalMonthThreeStructuresByState(
          locationBusinessStateObjPerGeoLevelMap);

      ColumnData totalMonthSixStructuresByState = getTotalStructuresByState(
          BusinessStatus.MONTH_SIX_COMPLETE,
          locationBusinessStateObjPerGeoLevelMap);

      columns.put(ENROLLED_COUNT,
          totalEnrolledStructuresByState);

      columns.put(NOT_ENROLLED_COUNT,
          getTotalStructuresByState(BusinessStatus.NOT_ENROLLED,
              locationBusinessStateObjPerGeoLevelMap));

      columns.put(ENROLLED_NOT_COMPLETE,
          getTotalStructuresByState(BusinessStatus.ENROLLED_NOT_COMPLETE,
              locationBusinessStateObjPerGeoLevelMap));

      columns.put(MONTH_THREE_COMPLETE_COUNT,
          totalMonthThreeStructuresByState);

      columns.put(MONTH_SIX_COMPLETE_COUNT,
          totalMonthSixStructuresByState);

      columns.put(CLUSTER_COUNT,
          ColumnData.builder().value(clusterCount.getLocationCount()).build());

      columns.put(VISITATION_COVERAGE,
          getFoundCoverage(totalStructuresTargetedCountObj,
              locationBusinessStateObjPerGeoLevelMap));
    } else if (instanceProperties.getClient().equals("zam")) {

      columns.put(TOTAL_STRUCTURES,
          getTotalStructuresCounts(totalStructuresCountObj,
              locationBusinessStateObjPerGeoLevelMap));

      columns.put(TOTAL_STRUCTURES_TARGETED,
          getTotalStructuresTargetedCountZam(totalStructuresTargetedCountObj,
              locationBusinessStateObjPerGeoLevelMap));


      ColumnData noOneHome = getTotalStructuresByState(
          BusinessStatus.NO_ONE_HOME,
          locationBusinessStateObjPerGeoLevelMap);
      columns.put(BusinessStatus.NO_ONE_HOME, noOneHome);

      ColumnData noAppropriateAdult = getTotalStructuresByState(
          NO_APPROPRIATE_ADULT_AVAILABLE,
          locationBusinessStateObjPerGeoLevelMap);
      columns.put(NO_APPROPRIATE_ADULT_AVAILABLE, noAppropriateAdult);

      ColumnData noEligible = getTotalStructuresByState(
          NOT_ELIGIBLE,
          locationBusinessStateObjPerGeoLevelMap);
      columns.put(NOT_ELIGIBLE, noEligible);

      ColumnData notVisited = getTotalStructuresByState(
          NOT_VISITED,
          locationBusinessStateObjPerGeoLevelMap);
      columns.put(NOT_VISITED, notVisited);

      ColumnData completed = getTotalStructuresByState(
          COMPLETE,
          locationBusinessStateObjPerGeoLevelMap);
      columns.put(COMPLETE, completed);


      columns.put(
          TOTAL_STRUCTURES_VISITED,
          getTotalStructuresFoundCountZam(totalStructuresTargetedCountObj,
              locationBusinessStateObjPerGeoLevelMap));


      columns.put(VISITATION_COVERAGE,
          getFoundCoverageZam(totalStructuresTargetedCountObj,
              locationBusinessStateObjPerGeoLevelMap));

      columns.put(COMPLETION_COVERAGE,
          getCompletionCoverageZam(totalStructuresTargetedCountObj,
              locationBusinessStateObjPerGeoLevelMap));
    } else {

      IndividualsByLocationProjection numberOfIndividualsByLocation = hdssCompoundsRepository.getNumberOfIndividualsByLocation(
          childLocation.getIdentifier());

      int individualsTested = hdssCompoundsRepository.getNumberOfTestedIndividualsByLocation(
          childLocation.getIdentifier());

      List<IndividualTaskBusinessStateByLocationProjection> businessStateByLocationProjections
          = hdssCompoundsRepository.getTaskBusinessStateCountsByLocation(plan.getIdentifier(),
          childLocation.getIdentifier());

      Integer totalIndexCases = businessStateByLocationProjections == null ? 0
          : businessStateByLocationProjections.stream().filter(item ->
                  List.of(INDEX_CASE_MEMBER, SECONDARY_INDEX_CASE_MEMBER).contains(item.getTitle()))
              .mapToInt(IndividualTaskBusinessStateByLocationProjection::getBusinessStatusCount)
              .sum();

      Integer totalIndexCasesConfirmed = businessStateByLocationProjections == null ? 0
          : businessStateByLocationProjections.stream().filter(item ->
                  List.of(INDEX_CASE_MEMBER, SECONDARY_INDEX_CASE_MEMBER).contains(item.getTitle()))
              .filter(item -> BusinessStatus.COMPLETE.equals(item.getBusinessStatus()))
              .mapToInt(IndividualTaskBusinessStateByLocationProjection::getBusinessStatusCount)
              .sum();

      Integer totalCases = businessStateByLocationProjections == null ? 0
          : businessStateByLocationProjections.stream()
              .mapToInt(IndividualTaskBusinessStateByLocationProjection::getBusinessStatusCount)
              .sum();

      columns.put(TOTAL_INDIVIDUALS, ColumnData.builder().value(
          numberOfIndividualsByLocation == null ? 0
              : numberOfIndividualsByLocation.getIndividualCount()).build());

      columns.put(TOTAL_INDEX_CASES, ColumnData.builder().value(totalIndexCases).build());

      columns.put(TOTAL_INDIVIDUALS_TESTED, ColumnData.builder().value(individualsTested).build());

      columns.put(TOTAL_CASES, ColumnData.builder().value(totalCases).build());

      double passiveIndexCaseDetectionPerc = 0;
      if (totalIndexCasesConfirmed > 0) {
        passiveIndexCaseDetectionPerc =
            (double) totalIndexCases / (double) totalIndexCasesConfirmed * 100;
      }

      columns.put(PASSIVE_CASE_PERCENTAGE,
          ColumnData.builder().value(passiveIndexCaseDetectionPerc).build());

      double racdMalariaPrevalence = 0;
      if (numberOfIndividualsByLocation != null
          && numberOfIndividualsByLocation.getIndividualCount() > 0) {
        racdMalariaPrevalence =
            (double) totalCases / (double) numberOfIndividualsByLocation.getIndividualCount() * 100;
      }

      columns.put(RACD_BASED_MALARIA_PREVALENCE,
          ColumnData.builder().value(racdMalariaPrevalence).build());
    }

    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  public List<RowData> getIRSFullCoverageStructureLevelData(Plan plan, Location childLocation) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(STRUCTURE_STATUS,
        getLocationBusinessState(plan, childLocation));
    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  private ColumnData getLocationBusinessState(Plan plan, Location location) {
    TaskBusinessStateTracker locationBusinessState = locationBusinessStatusService.findLocationBusinessState(
        plan.getLocationHierarchy().getIdentifier(), location.getIdentifier(),
        plan.getIdentifier());

    ColumnData columnData = DashboardUtils.getStringValueColumnData();

    if (locationBusinessState != null) {
      columnData.setValue(locationBusinessState.getTaskBusinessStatus());
    }

    return columnData;
  }

  private ColumnData getFoundCoverage(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double foundStructures = (double) getTotalStructuresFoundCount(totalStructuresTargetedCountObj,
        locationBusinessStateObjPerGeoLevelMap).getValue();
    double targetedStructures = (double) getTotalStructuresTargetedCount(
        totalStructuresTargetedCountObj, locationBusinessStateObjPerGeoLevelMap).getValue();
    if (targetedStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((foundStructures / targetedStructures) * 100);
    }
    columnData.setMeta("Visited Structures: " + foundStructures + " / " + "Targeted Structure: "
        + targetedStructures);
    return columnData;
  }

  private ColumnData getFoundCoverageZam(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double foundStructures = (double) getTotalStructuresFoundCountZam(totalStructuresTargetedCountObj,
        locationBusinessStateObjPerGeoLevelMap).getValue();
    double targetedStructures = (double) getTotalStructuresTargetedCountZam(
        totalStructuresTargetedCountObj, locationBusinessStateObjPerGeoLevelMap).getValue();
    if (targetedStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((foundStructures / targetedStructures) * 100);
    }
    columnData.setMeta("Visited Structures: " + foundStructures + " / " + "Targeted Structure: "
        + targetedStructures);
    return columnData;
  }


  private ColumnData getCompletionCoverageZam(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);

    double completedStructuresCount;
    LocationBusinessStateCount completedStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        COMPLETE);
    if (completedStructuresCountObjCount != null) {
      completedStructuresCount = completedStructuresCountObjCount.getLocationCount();
    } else {
      completedStructuresCount = 0L;
    }

    double targetedStructures = (double) getTotalStructuresTargetedCountZam(
        totalStructuresTargetedCountObj, locationBusinessStateObjPerGeoLevelMap).getValue();
    if (targetedStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((completedStructuresCount / targetedStructures) * 100);
    }
    columnData.setMeta("Complete Structures: " + completedStructuresCount + " / " + "Targeted Structure: "
        + targetedStructures);
    return columnData;
  }
//  private ColumnData getDistributionCoverage(long totalStructuresTargetedCountObj,
//      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {
//    ColumnData columnData = new ColumnData();
//    columnData.setIsPercentage(true);
//    double foundStructures = (double) getTotalStructuresFoundCount(totalStructuresTargetedCountObj,
//        locationBusinessStateObjPerGeoLevelMap).getValue();
//    double mdaComplete = (double) getTotalStructuresMdaCompleteOrPartiallyCompleted(
//        locationBusinessStateObjPerGeoLevelMap).getValue();
//    if (foundStructures == 0) {
//      columnData.setValue(0d);
//    } else {
//      columnData.setValue((mdaComplete / foundStructures) * 100);
//    }
//    columnData.setMeta("MDA Complete: " + mdaComplete + " / " + "Visited: " + foundStructures);
//    return columnData;
//  }

  private ColumnData getTotalStructuresByState(String businessState,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    ColumnData columnData = new ColumnData();

    double completedStructuresCount;
    LocationBusinessStateCount completedStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        businessState);
    if (completedStructuresCountObjCount != null) {
      completedStructuresCount = completedStructuresCountObjCount.getLocationCount();
    } else {
      completedStructuresCount = 0L;
    }

    columnData.setValue(completedStructuresCount);

    return columnData;
  }

  private ColumnData getTotalEnrolledStructuresByState(
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    String enrolled = BusinessStatus.ENROLLED;
    String monthThree = BusinessStatus.MONTH_THREE_COMPLETE;
    String monthSix = BusinessStatus.MONTH_SIX_COMPLETE;

    ColumnData columnData = new ColumnData();

    double enrolledStructuresCount;
    LocationBusinessStateCount enrolledStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        enrolled);
    if (enrolledStructuresCountObjCount != null) {
      enrolledStructuresCount = enrolledStructuresCountObjCount.getLocationCount();
    } else {
      enrolledStructuresCount = 0L;
    }

    double monthThreeStructuresCount;
    LocationBusinessStateCount monthThreeStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        monthThree);
    if (monthThreeStructuresCountObjCount != null) {
      monthThreeStructuresCount = monthThreeStructuresCountObjCount.getLocationCount();
    } else {
      monthThreeStructuresCount = 0L;
    }

    double monthSixStructuresCount;
    LocationBusinessStateCount monthSixStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        monthSix);
    if (monthSixStructuresCountObjCount != null) {
      monthSixStructuresCount = monthSixStructuresCountObjCount.getLocationCount();
    } else {
      monthSixStructuresCount = 0L;
    }

    double totalEnrolled =
        enrolledStructuresCount + monthThreeStructuresCount + monthSixStructuresCount;

    columnData.setValue(totalEnrolled);

    return columnData;
  }

  private ColumnData getTotalMonthThreeStructuresByState(
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    String monthThree = BusinessStatus.MONTH_THREE_COMPLETE;
    String monthSix = BusinessStatus.MONTH_SIX_COMPLETE;

    ColumnData columnData = new ColumnData();

    double monthThreeStructuresCount;
    LocationBusinessStateCount monthThreeStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        monthThree);
    if (monthThreeStructuresCountObjCount != null) {
      monthThreeStructuresCount = monthThreeStructuresCountObjCount.getLocationCount();
    } else {
      monthThreeStructuresCount = 0L;
    }

    double monthSixStructuresCount;
    LocationBusinessStateCount monthSixStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        monthSix);
    if (monthSixStructuresCountObjCount != null) {
      monthSixStructuresCount = monthSixStructuresCountObjCount.getLocationCount();
    } else {
      monthSixStructuresCount = 0L;
    }

    double totalMonthThree = monthThreeStructuresCount + monthSixStructuresCount;

    columnData.setValue(totalMonthThree);

    return columnData;
  }

  private ColumnData getTotalStructuresByRoundRefuse(
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    ColumnData columnData = new ColumnData();

    double refusedCount;

    LocationBusinessStateCount refused = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.REFUSED);

    LocationBusinessStateCount roundOneRefused = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.ROUND_ONE_REFUSED);

    LocationBusinessStateCount roundTwoRefused = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.ROUND_TWO_REFUSED);

    LocationBusinessStateCount roundThreeRefused = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.ROUND_THREE_REFUSED);

    if (refused != null) {
      refusedCount = refused.getLocationCount();
    } else {
      refusedCount = 0L;
    }

    if (roundOneRefused != null) {
      refusedCount += roundOneRefused.getLocationCount();
    }

    if (roundTwoRefused != null) {
      refusedCount += roundTwoRefused.getLocationCount();
    }

    if (roundThreeRefused != null) {
      refusedCount += roundThreeRefused.getLocationCount();
    }

    columnData.setValue(refusedCount);

    return columnData;
  }


  private ColumnData getTotalStructuresCounts(Long totalStructuresCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    double totalStructuresCount = 0;
    if (totalStructuresCountObj != null) {
      totalStructuresCount = totalStructuresCountObj;
    }

    Long notEligibleStructuresCountObj = null;

    LocationBusinessStateCount notEligibleStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.NOT_ELIGIBLE);

    if (notEligibleStructuresCountObjCount != null) {
      notEligibleStructuresCountObj = notEligibleStructuresCountObjCount.getLocationCount();
    }

    double notEligibleStructuresCount = 0;
    if (notEligibleStructuresCountObj != null) {
      notEligibleStructuresCount = notEligibleStructuresCountObj;
    }

    double totalStructuresExcludingNotEligible =
        totalStructuresCount - notEligibleStructuresCount;

    ColumnData totalStructuresColumnData = new ColumnData();
    totalStructuresColumnData.setValue(totalStructuresExcludingNotEligible);
    totalStructuresColumnData.setIsPercentage(false);
    return totalStructuresColumnData;
  }

  private ColumnData getTotalStructuresTargetedCountZam(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    double totalStructuresInPlanLocationCount = 0;
    totalStructuresInPlanLocationCount = totalStructuresTargetedCountObj;


    double totalStructuresInTargetedCount =
        totalStructuresInPlanLocationCount ;

    ColumnData totalStructuresTargetedColumnData = new ColumnData();
    totalStructuresTargetedColumnData.setValue(totalStructuresInTargetedCount);
    totalStructuresTargetedColumnData.setIsPercentage(false);
    return totalStructuresTargetedColumnData;
  }


  private ColumnData getTotalStructuresTargetedCount(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    double totalStructuresInPlanLocationCount = 0;
    totalStructuresInPlanLocationCount = totalStructuresTargetedCountObj;

    Long notEligibleStructuresCountObj = null;
    LocationBusinessStateCount notEligibleStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.NOT_ELIGIBLE);

    if (notEligibleStructuresCountObjCount != null) {
      notEligibleStructuresCountObj = notEligibleStructuresCountObjCount.getLocationCount();
    }

    double notEligibleStructuresCount = 0;
    if (notEligibleStructuresCountObj != null) {
      notEligibleStructuresCount = notEligibleStructuresCountObj;
    }

    double totalStructuresInTargetedCount =
        totalStructuresInPlanLocationCount - notEligibleStructuresCount;

    ColumnData totalStructuresTargetedColumnData = new ColumnData();
    totalStructuresTargetedColumnData.setValue(totalStructuresInTargetedCount);
    totalStructuresTargetedColumnData.setIsPercentage(false);
    return totalStructuresTargetedColumnData;
  }

  private ColumnData getTotalStructuresFoundCount(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);

    Long notEligibleStructuresCountObj = null;
    LocationBusinessStateCount notEligibleStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.NOT_ELIGIBLE);

    if (notEligibleStructuresCountObjCount != null) {
      notEligibleStructuresCountObj = notEligibleStructuresCountObjCount.getLocationCount();
    }

    double notEligibleStructuresCount = 0;
    if (notEligibleStructuresCountObj != null) {
      notEligibleStructuresCount = notEligibleStructuresCountObj;
    }

    double totalStructuresInTargetedCount =
        (double) totalStructuresTargetedCountObj - notEligibleStructuresCount;

    Long notVisitedStructuresCountObj = null;
    LocationBusinessStateCount notVisitedStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.NOT_VISITED);

    if (notVisitedStructuresCountObjCount != null) {
      notVisitedStructuresCountObj = notVisitedStructuresCountObjCount.getLocationCount();
    }

    double notVisitedStructuresCount = 0;
    if (notVisitedStructuresCountObj != null) {
      notVisitedStructuresCount = notVisitedStructuresCountObj;
    }

    double found = totalStructuresInTargetedCount - notVisitedStructuresCount;

    columnData.setValue(found);

    return columnData;
  }

  private ColumnData getTotalStructuresFoundCountZam(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);


    Long notVisitedStructuresCountObj = null;
    LocationBusinessStateCount notVisitedStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.NOT_VISITED);

    if (notVisitedStructuresCountObjCount != null) {
      notVisitedStructuresCountObj = notVisitedStructuresCountObjCount.getLocationCount();
    }

    double notVisitedStructuresCount = 0;
    if (notVisitedStructuresCountObj != null) {
      notVisitedStructuresCount = notVisitedStructuresCountObj;
    }

    double found = totalStructuresTargetedCountObj - notVisitedStructuresCount;

    columnData.setValue(found);

    return columnData;
  }

  private ColumnData getTotalStructuresCompleteCount(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);

    Long completeStructuresCountObj = null;
    LocationBusinessStateCount completeStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.COMPLETE);

    if (completeStructuresCountObjCount != null) {
      completeStructuresCountObj = completeStructuresCountObjCount.getLocationCount();

      columnData.setValue(completeStructuresCountObj);
    }

    return columnData;
  }

  public FeatureSetResponse getFeatureSetResponse(UUID parentIdentifier,
      List<PlanLocationDetails> locationDetails, Map<UUID, RowData> rowDataMap,
      String reportLevel) {
    FeatureSetResponse response = new FeatureSetResponse();
    response.setType("FeatureCollection");
    List<LocationResponse> locationResponses = locationDetails.stream()
        .map(loc -> LocationResponseFactory.fromPlanLocationDetails(loc, parentIdentifier))
        .collect(Collectors.toList());

    log.info("Row Map Data: {}", rowDataMap);

    locationResponses = setGeoJsonProperties(rowDataMap, locationResponses);
    if ("uw".equals(instanceProperties.getClient())) {
      response.setDefaultDisplayColumn(
          dashboardProperties.getUwSurveyDefaultDisplayColumns().getOrDefault(reportLevel, null));
    } else if ("zam".equals(instanceProperties.getClient())) {
      response.setDefaultDisplayColumn(
          dashboardProperties.getZamSurveyDefaultDisplayColumns().getOrDefault(reportLevel, null));
    } else {
      response.setDefaultDisplayColumn(
          dashboardProperties.getNihGhaSurveyDefaultDisplayColumns()
              .getOrDefault(reportLevel, null));
    }

    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }

  public List<RowData> getNihGhaStructureData(Plan plan, @Nullable Location childLocation,
      MdaLiteReportType type, Location parentLocation) {

    List<HdssEventDataProjection> eventDataForLocationAndPlan = hdssCompoundsRepository.getEventDataForLocationAndPlan(
        parentLocation.getIdentifier(), plan.getIdentifier());

    List<IndividualsPerCompoundByLocationProjection> numberOfIndividualsPerCompoundByLocation
        = hdssCompoundsRepository.getNumberOfIndividualsPerCompoundByLocation(
        parentLocation.getIdentifier());

    Map<String, IndividualsPerCompoundByLocationProjection> individualsPerCompoundByLocationProjectionMap = numberOfIndividualsPerCompoundByLocation.stream()
        .collect(Collectors.toMap(
            IndividualsPerCompoundByLocationProjection::getCompound, num -> num, (a, b) -> b));

    List<RowData> collect = eventDataForLocationAndPlan.stream()
        .map(eventDataForLocationAndPlanItem -> getOperationalData(eventDataForLocationAndPlanItem,
            individualsPerCompoundByLocationProjectionMap))
        .map(stringColumnDataMap -> {
          RowData rowData = new RowData();
          rowData.setLocationIdentifier(
              UUID.fromString((String) stringColumnDataMap.get(LOCATION).getValue()));
          rowData.setColumnDataMap(stringColumnDataMap);
          rowData.setLocationName((String) stringColumnDataMap.get("Compound").getValue());
          return rowData;
        })
        .collect(Collectors.toList());
    return collect;
  }

  public List<RowData> getNihGhaBelowHighestLevelData(Plan plan, @Nullable Location childLocation,
      MdaLiteReportType type, Location parentLocation) {

    List<GdrsCountsProjection> arr = new ArrayList<>();

    List<GdrsCountsProjection> rcdCounts = hdssCompoundsRepository.getRCDCounts(
        parentLocation.getGeographicLevel().getName());

    List<GdrsCountsProjection> passiveCounts = hdssCompoundsRepository.getPassiveCounts(
        parentLocation.getGeographicLevel().getName());

    List<GdrsCountsProjection> indexCounts = hdssCompoundsRepository.getIndexCounts(
        parentLocation.getGeographicLevel().getName());

    arr.addAll(rcdCounts);
    arr.addAll(passiveCounts);
    arr.addAll(indexCounts);

    Set<GDRSLocation> locations = arr.stream().map(
            gdrsCountsProjection -> new GDRSLocation(gdrsCountsProjection.getParIdentifier(),
                gdrsCountsProjection.getParName()))
        .collect(Collectors.toSet());

    Map<String, Integer> totalRCDTested = rcdCounts.stream()
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalRCDPositive = rcdCounts.stream()
        .filter(rcdCount -> rcdCount.getRdt().equals("positive"))
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalPassiveTested = passiveCounts.stream()
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalPassivePositive = passiveCounts.stream()
        .filter(rcdCount -> rcdCount.getRdt().equals("positive"))
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalIndex = indexCounts.stream()
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    List<IndividualsPerCompoundByLocationProjection> numberOfIndividualsPerCompoundByLocation
        = hdssCompoundsRepository.getListOfNumberOfIndividualsByLocation(
        parentLocation.getIdentifier());

    Map<String, IndividualsPerCompoundByLocationProjectionObj> individualsByLocationProjectionMap = numberOfIndividualsPerCompoundByLocation.stream()
        .map(item -> IndividualsPerCompoundByLocationProjectionObj
            .builder()
            .individualCount(item.getIndividualCount())
            .locationIdentifier(item.getLocationIdentifier())
            .build())
        .collect(Collectors.toMap(
            IndividualsPerCompoundByLocationProjectionObj::getLocationIdentifier, num -> num,
            (a, b) -> b));

    log.info("IndividualsPerCompoundByLocationProjectionObj {} ",
        individualsByLocationProjectionMap);

    GRDSCountsBelow grdsCountsBelow = new GRDSCountsBelow(totalRCDTested, totalRCDPositive,
        totalPassiveTested, totalPassivePositive, totalIndex, individualsByLocationProjectionMap);

    List<RowData> collect = locations.stream()
        .map(location -> getOperationalDataBelowHighestLevel(
            location
            , grdsCountsBelow
        ))
        .map(stringColumnDataMap -> {
          RowData rowData = new RowData();
          rowData.setLocationIdentifier(
              UUID.fromString((String) stringColumnDataMap.get(LOCATION).getValue()));
          rowData.setColumnDataMap(stringColumnDataMap);
          rowData.setLocationName((String) stringColumnDataMap.get(LOCATION_NAME).getValue());
          return rowData;
        })
        .collect(Collectors.toList());
    log.info("Rows {}", collect.size());
    return collect;
  }

  @Data
  @AllArgsConstructor
  public static class GDRSLocation {

    private String locationId;
    private String locationName;
  }

  @Data
  @AllArgsConstructor
  public static class GRDSCounts {

    private Map<String, Integer> totalRCDTested;
    private Map<String, Integer> totalRCDPositive;
    private Map<String, Integer> totalPassiveTested;
    private Map<String, Integer> totalPassivePositive;
    private Map<String, Integer> totalIndex;
    private IndividualsByLocationProjection numberOfIndividualsByLocation;
  }

  @Data
  @AllArgsConstructor
  public static class GRDSCountsBelow {

    private Map<String, Integer> totalRCDTested;
    private Map<String, Integer> totalRCDPositive;
    private Map<String, Integer> totalPassiveTested;
    private Map<String, Integer> totalPassivePositive;
    private Map<String, Integer> totalIndex;
    private Map<String, IndividualsPerCompoundByLocationProjectionObj> individualsByLocationProjectionMap;
//    private IndividualsByLocationProjection numberOfIndividualsByLocation;
  }

  public List<RowData> getNihGhaHighestLevelData(Plan plan, @Nullable Location childLocation,
      MdaLiteReportType type, Location parentLocation) {

    List<GdrsCountsProjection> arr = new ArrayList<>();

    List<GdrsCountsProjection> rcdCounts = hdssCompoundsRepository.getHighestLevelRCDCounts();

    List<GdrsCountsProjection> passiveCounts = hdssCompoundsRepository.getHighestLevelPassiveCounts();

    List<GdrsCountsProjection> indexCounts = hdssCompoundsRepository.getHighestLevelIndexCounts();

    arr.addAll(rcdCounts);
    arr.addAll(passiveCounts);
    arr.addAll(indexCounts);

    Set<GDRSLocation> locations = arr.stream().map(
            gdrsCountsProjection -> new GDRSLocation(gdrsCountsProjection.getParIdentifier(),
                gdrsCountsProjection.getParName()))
        .collect(Collectors.toSet());

    Map<String, Integer> totalRCDTested = rcdCounts.stream()
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalRCDPositive = rcdCounts.stream()
        .filter(rcdCount -> rcdCount.getRdt().equals("positive"))
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalPassiveTested = passiveCounts.stream()
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalPassivePositive = passiveCounts.stream()
        .filter(rcdCount -> rcdCount.getRdt().equals("positive"))
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    Map<String, Integer> totalIndex = indexCounts.stream()
        .collect(Collectors.groupingBy(GdrsCountsProjection::getParIdentifier,
            Collectors.summingInt(GdrsCountsProjection::getCount)
        ));

    IndividualsByLocationProjection numberOfIndividualsByLocation = hdssCompoundsRepository.getNumberOfIndividualsByLocation(
        childLocation.getIdentifier());

    GRDSCounts grdsCounts = new GRDSCounts(totalRCDTested, totalRCDPositive, totalPassiveTested,
        totalPassivePositive, totalIndex, numberOfIndividualsByLocation);

    List<RowData> collect = locations.stream()
        .map(location -> getOperationalDataHighestLevel(
            location
            , grdsCounts
        ))
        .map(stringColumnDataMap -> {
          RowData rowData = new RowData();
          rowData.setLocationIdentifier(
              UUID.fromString((String) stringColumnDataMap.get(LOCATION).getValue()));
          rowData.setColumnDataMap(stringColumnDataMap);
          rowData.setLocationName((String) stringColumnDataMap.get(LOCATION_NAME).getValue());
          return rowData;
        })
        .collect(Collectors.toList());
    log.info("Rows {}", collect.size());
    return collect;
  }

  private Map<String, ColumnData> getOperationalData(
      HdssEventDataProjection hdssEventDataProjection,
      Map<String, IndividualsPerCompoundByLocationProjection> individualsPerCompoundByLocationProjectionMap) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(LOCATION,
        new ColumnData().setIsHidden(true).setDataType("string").setValue(
            (hdssEventDataProjection == null ? 0
                : hdssEventDataProjection.getLocationIdentifier())));

    columns.put(COMPOUND,
        new ColumnData().setDataType("string").setValue(
            (hdssEventDataProjection == null ? 0
                : hdssEventDataProjection.getCompound())));

    columns.put(TOTAL_INDIVIDUALS, new ColumnData().setValue(
        individualsPerCompoundByLocationProjectionMap != null && hdssEventDataProjection != null
            && hdssEventDataProjection.getCompound() != null &&
            individualsPerCompoundByLocationProjectionMap.containsKey(
                hdssEventDataProjection.getCompound()) ?
            individualsPerCompoundByLocationProjectionMap.get(
                hdssEventDataProjection.getCompound()).getIndividualCount() : 0));

    columns.put(TOTAL_INDEX_CASES,
        new ColumnData().setValue(
            (hdssEventDataProjection == null ? 0
                : hdssEventDataProjection.getPassivePositive())));

    columns.put(TOTAL_INDIVIDUALS_TESTED,
        new ColumnData().setValue(
            (hdssEventDataProjection == null ? 0
                : hdssEventDataProjection.getTotalTested())));

    columns.put(TOTAL_CASES,
        new ColumnData().setValue(
            (hdssEventDataProjection == null ? 0
                : hdssEventDataProjection.getTotalCases())));

    int indexCases = 0;
    int rcdCases = 0;
    int totalCases = 0;
    int totalIndividuals = 0;

    if (hdssEventDataProjection != null) {
      indexCases = hdssEventDataProjection.getPassivePositive();
      rcdCases = hdssEventDataProjection.getRcdPositive();
      totalCases = hdssEventDataProjection.getTotalCases();
    }

    if (individualsPerCompoundByLocationProjectionMap != null &&
        hdssEventDataProjection != null && hdssEventDataProjection.getCompound() != null &&
        individualsPerCompoundByLocationProjectionMap.containsKey(
            hdssEventDataProjection.getCompound())) {
      totalIndividuals = individualsPerCompoundByLocationProjectionMap.get(
          hdssEventDataProjection.getCompound()).getIndividualCount();
    }
    double passiveIndexCaseDetectionRation =
        rcdCases > 0 ? ((double) indexCases / (double) rcdCases) * 100 : 0;

    String passiveIndexCaseDetectionRatioMeta = String.format("index cases (%s) / rcd cases (%s)",
        indexCases, rcdCases);

    columns.put(PASSIVE_CASE_PERCENTAGE,
        new ColumnData().setValue(
                passiveIndexCaseDetectionRation)
            .setMeta(passiveIndexCaseDetectionRatioMeta));

    double rcdBasedMalariaPrevalence =
        totalIndividuals > 0 ? ((double) totalCases / (double) totalIndividuals) * 100 : 0;

    String rcdBasedMalariaPrevalenceMeta = String.format(
        "total cases (%s) / total individuals (%s)", totalCases, totalIndividuals);

    columns.put(RACD_BASED_MALARIA_PREVALENCE,
        new ColumnData().setValue(
            rcdBasedMalariaPrevalence).setMeta(rcdBasedMalariaPrevalenceMeta));

    return columns;
  }

  private Map<String, ColumnData> getOperationalDataBelowHighestLevel(
      GDRSLocation location
      ,
      GRDSCountsBelow grdsCountsBelow
  ) {

    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(LOCATION,
        new ColumnData().setIsHidden(true).setDataType("string").setValue(
            location.getLocationId()));

    columns.put(LOCATION_NAME,
        new ColumnData().setIsHidden(true).setDataType("string").setValue(
            location.getLocationName()));

    int totalIndividuals = 0;

    if (grdsCountsBelow.getIndividualsByLocationProjectionMap() != null
        && grdsCountsBelow.getIndividualsByLocationProjectionMap()
        .containsKey(location.getLocationId())) {
      totalIndividuals = grdsCountsBelow.individualsByLocationProjectionMap.get(
          location.getLocationId()).getIndividualCount();
    }

    columns.put(TOTAL_INDIVIDUALS, new ColumnData().setValue(
        totalIndividuals));

    Integer totalIndexObj = grdsCountsBelow.getTotalIndex().get(location.getLocationId());
    Integer totalRCDObj = grdsCountsBelow.getTotalRCDPositive().get(location.getLocationId());
    Integer totalPassiveObj = grdsCountsBelow.getTotalPassivePositive()
        .get(location.getLocationId());

    Integer rcdTestedObj = grdsCountsBelow.getTotalRCDTested().get(location.getLocationId());
    Integer passiveTestedObj = grdsCountsBelow.getTotalPassiveTested()
        .get(location.getLocationId());

    int totalCases = 0;
    int totalPassive = 0;
    int totalPositive = 0;
    int totalRCD = 0;
    int totalIndex = 0;
    int totalRcdTested = 0;
    int totalPassiveTested = 0;
    if (totalIndexObj != null) {
      totalIndex = totalIndexObj;
    }
    if (totalPassiveObj != null) {
      totalPassive = totalPassiveObj;
    }
    if (totalRCDObj != null) {
      totalRCD = totalRCDObj;
    }
    if (rcdTestedObj != null) {
      totalRcdTested = rcdTestedObj;
    }
    if (passiveTestedObj != null) {
      totalPassiveTested = passiveTestedObj;
    }

    totalCases = totalRCD + totalPassive;

    String totalCasesMeta = "rcd:  " + totalRCD + " passive: " + totalPassive;

    columns.put(TOTAL_INDEX_CASES,
        new ColumnData().setValue(totalPassive));

    int totalTested =
        totalRcdTested + totalPassiveTested;

    columns.put(TOTAL_INDIVIDUALS_TESTED,
        new ColumnData().setValue(
            totalTested));

    columns.put(TOTAL_RCD_CASES,
        new ColumnData().setValue(
            totalRCD));

    columns.put(TOTAL_CASES,
        new ColumnData().setMeta(totalCasesMeta).setValue(
            totalCases));

    double passiveIndexCaseDetectionRation =
        totalRCD > 0 ? ((double) totalPassive / (double) totalRCD) : 0;

    String passiveIndexCaseDetectionRatioMeta = String.format("index cases (%s) / rcd cases (%s)",
        totalPassive, totalRCD);

    columns.put(PASSIVE_CASE_PERCENTAGE,
        new ColumnData().setValue(
                passiveIndexCaseDetectionRation)
            .setMeta(passiveIndexCaseDetectionRatioMeta));

    double rcdBasedMalariaPrevalence =
        totalIndividuals > 0 ? ((double) totalCases / (double) totalIndividuals) * 100 : 0;

    String rcdBasedMalariaPrevalenceMeta = String.format(
        "total cases (%s) / total individuals (%s)", totalCases, totalIndividuals);

    columns.put(RACD_BASED_MALARIA_PREVALENCE,
        new ColumnData().setValue(
                rcdBasedMalariaPrevalence).setIsPercentage(true)
            .setMeta(rcdBasedMalariaPrevalenceMeta));

    log.info("Columns {}", columns);
    return columns;

  }

  private Map<String, ColumnData> getOperationalDataHighestLevel(
      GDRSLocation location
      ,
      GRDSCounts grdsCounts
  ) {

    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(LOCATION,
        new ColumnData().setIsHidden(true).setDataType("string").setValue(
            location.getLocationId()));

    columns.put(LOCATION_NAME,
        new ColumnData().setIsHidden(true).setDataType("string").setValue(
            location.getLocationName()));

    columns.put(TOTAL_INDIVIDUALS, new ColumnData().setValue(
        grdsCounts.getNumberOfIndividualsByLocation() != null
            ? grdsCounts.getNumberOfIndividualsByLocation().getIndividualCount() : 0));

    int totalIndex = 0;
    int totalRCD = 0;
    int totalPassive = 0;
    int totalPassiveTested = 0;
    int totalRcdTested = 0;

    Integer totalIndexObj = grdsCounts.getTotalIndex().get(location.getLocationId());
    Integer rcdTestedObj = grdsCounts.getTotalRCDTested().get(location.getLocationId());
    Integer passiveTestedObj = grdsCounts.getTotalPassiveTested().get(location.getLocationId());
    Integer totalRCDPositiveObj = grdsCounts.getTotalRCDPositive().get(location.getLocationId());
    Integer totalPassiveObj = grdsCounts.getTotalPassivePositive().get(location.getLocationId());

    if (totalIndexObj != null) {
      totalIndex = totalIndexObj;
    }
    if (totalPassiveObj != null) {
      totalPassive = totalPassiveObj;
    }
    if (totalRCDPositiveObj != null) {
      totalRCD = totalRCDPositiveObj;
    }
    if (rcdTestedObj != null) {
      totalRcdTested = rcdTestedObj;
    }
    if (passiveTestedObj != null) {
      totalPassiveTested = passiveTestedObj;
    }

    int totalTested =
        totalPassiveTested +
            totalRcdTested;

    int totalCases =
        totalRCD +
            totalPassive;

    String totalCasesMeta = "rcd:  " + totalRCD + " passive: " + totalPassive;

    columns.put(TOTAL_INDEX_CASES,
        new ColumnData().setValue(totalPassive));

    columns.put(TOTAL_INDIVIDUALS_TESTED,
        new ColumnData().setValue(
            totalTested));

    columns.put(TOTAL_RCD_CASES,
        new ColumnData().setValue(
            totalRCD));

    columns.put(TOTAL_CASES,
        new ColumnData().setMeta(totalCasesMeta).setValue(
            totalCases));

    double passiveIndexCaseDetectionRation =
        totalRCD > 0 ? ((double) totalIndex / (double) totalRCD) : 0;

    String passiveIndexCaseDetectionRatioMeta = String.format("index cases (%s) / rcd cases (%s)",
        totalIndex, totalRCD);

    columns.put(PASSIVE_CASE_PERCENTAGE,
        new ColumnData().setValue(
                passiveIndexCaseDetectionRation)

            .setMeta(passiveIndexCaseDetectionRatioMeta));

    int totalIndividuals = 0;
    if (grdsCounts.getNumberOfIndividualsByLocation() != null) {
      totalIndividuals = grdsCounts.getNumberOfIndividualsByLocation().getIndividualCount();
    }

    double rcdBasedMalariaPrevalence =
        totalIndividuals > 0 ? ((double) totalCases / (double) totalIndividuals) * 100 : 0;

    String rcdBasedMalariaPrevalenceMeta = String.format(
        "total cases (%s) / total individuals (%s)", totalCases, totalIndividuals);

    columns.put(RACD_BASED_MALARIA_PREVALENCE,
        new ColumnData().setValue(
                rcdBasedMalariaPrevalence).setIsPercentage(true)
            .setMeta(rcdBasedMalariaPrevalenceMeta));

    log.info("Columns {}", columns);
    return columns;
  }

  private List<LocationResponse> setGeoJsonProperties(Map<UUID, RowData> rowDataMap,
      List<LocationResponse> locationResponses) {
    return locationResponses.stream().filter(loc -> rowDataMap.containsKey(loc.getIdentifier()))
        .peek(loc -> {
          loc.getProperties()
              .setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
          loc.getProperties().setId(loc.getIdentifier().toString());
          if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(VISITATION_COVERAGE)
              != null) {
            loc.getProperties().setFoundCoverage(
                rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(VISITATION_COVERAGE)
                    .getValue());
          }
          if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
              .get(STRUCTURE_STATUS) != null) {
            String businessStatus = (String) rowDataMap.get(loc.getIdentifier())
                .getColumnDataMap()
                .get(STRUCTURE_STATUS).getValue();
            loc.getProperties().setBusinessStatus(
                businessStatus == null ? "No State" : businessStatus);
            loc.getProperties().setStatusColor(getBusinessStatusColor(businessStatus));
          }

        }).collect(Collectors.toList());
  }
}


