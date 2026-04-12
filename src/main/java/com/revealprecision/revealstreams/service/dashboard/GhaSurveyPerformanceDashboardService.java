package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.enums.ReportTypeEnum.SURVEY;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.ALL_OTHER_LEVELS;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.CDD_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.DIRECTLY_ABOVE_STRUCTURE_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.IS_ON_PLAN_TARGET;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.LOWEST_LITE_TOUCH_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.STRUCTURE_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.SUPERVISOR_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.WITHIN_STRUCTURE_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_END_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_HOURS_WORKED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_START_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.DAYS_WORKED;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.enums.ReportTypeEnum;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.models.amdr.AmdrPerformanceAggregatedData;
import com.revealprecision.revealstreams.models.amdr.AmdrPerformanceAggregatedDataString;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.projection.amdr.AmdrAggregatedDataProjection;
import com.revealprecision.revealstreams.persistence.projection.amdr.AmdrPerformanceDataProjection;
import com.revealprecision.revealstreams.persistence.repository.amdr.AmdrRepository;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.props.InstanceProperties;
import com.revealprecision.revealstreams.service.LocationService;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhaSurveyPerformanceDashboardService {

  private final DashboardProperties dashboardProperties;

  private final AmdrRepository amdrRepository;

  private final ObjectMapper objectMapper;

  public static final String FOUND = "Number of Structures Found";
  public static final String DAY = "Day";
  public static final String SPRAYED = "Number of Structures Sprayed";
  public static final String AVERAGE_STRUCTURES = "Average Structures Per Day";
  public static final String DATA_QUALITY_CHECK = "Data Quality Check";
  public static final String SPRAYED_DIFF = "Sprayed Difference";
  public static final String FOUND_DIFF = "Found Difference";
  public static final String CHECKED = "Data Quality Check";
  public static final String NOT_SPRAYED = "Number of Structures Not Sprayed";
  public static final String NOT_SPRAYED_TOTAL = "Number of Structures Not Sprayed Total";
  public static final String NOT_SPRAYED_REFUSED = "Number of Structures Not Sprayed Refused";
  public static final String NOT_SPRAYED_REFUSED_OTHER = "Number of Structures Not Sprayed Other";
  public static final String BOTTLES_USED = "Insecticide Used";
  public static final String BOTTLES_USAGE_RATE = "Insecticide Usage Rate";
  public static final String NUMBER_OF_DAYS_TEAMS_IN_THE_FIELD = "Number of days Teams in the Field";

  public static final Map<String, Set<String>> columnOrder = new HashMap<>();
  private final LocationService locationService;
  private final InstanceProperties instanceProperties;

  static {
    Set<String> districtCols = new LinkedHashSet<>();
    districtCols.add(NUMBER_OF_DAYS_TEAMS_IN_THE_FIELD);
    districtCols.add(AVERAGE_STRUCTURES);
    districtCols.add(AVERAGE_START_TIME);
    districtCols.add(AVERAGE_END_TIME);
    districtCols.add(AVERAGE_HOURS_WORKED);
    districtCols.add(BOTTLES_USAGE_RATE);
    districtCols.add(DATA_QUALITY_CHECK);
    columnOrder.put("district", districtCols);

    Set<String> deviceUserCols = new LinkedHashSet<>();
    deviceUserCols.add(DAYS_WORKED);
    deviceUserCols.add(FOUND);
    deviceUserCols.add(SPRAYED);
    deviceUserCols.add(NOT_SPRAYED);
    deviceUserCols.add(AVERAGE_STRUCTURES);
    deviceUserCols.add(AVERAGE_START_TIME);
    deviceUserCols.add(AVERAGE_END_TIME);
    deviceUserCols.add(DATA_QUALITY_CHECK);
    columnOrder.put("deviceUser", deviceUserCols);

    Set<String> fieldWorkerCols = new LinkedHashSet<>();
    fieldWorkerCols.add(DAYS_WORKED);
    fieldWorkerCols.add(FOUND);
    fieldWorkerCols.add(AVERAGE_STRUCTURES);
    fieldWorkerCols.add(SPRAYED);
    fieldWorkerCols.add(NOT_SPRAYED);
    fieldWorkerCols.add(AVERAGE_START_TIME);
    fieldWorkerCols.add(AVERAGE_END_TIME);
    columnOrder.put("fieldWorker", fieldWorkerCols);
  }

  public List<RowData> getPerformanceColumnData(
      Plan plan, String parentIdentifierString) {

    UUID parentIdentifier = null;
    Location parentLocation = null;
    if (parentIdentifierString != null) {
      try {
        parentIdentifier = UUID.fromString(parentIdentifierString);
      } catch (IllegalArgumentException illegalArgumentException) {
        parentIdentifier = UUID.fromString(parentIdentifierString.split("_")[2]);
      }
    }

    if (parentIdentifier != null) {
      parentLocation = locationService.findByIdentifier(parentIdentifier);
    }

    List<PlanLocationDetails> locationDetails = getPlanLocationDetails(
        plan.getIdentifier(), parentIdentifier, plan, parentLocation);
    String reportLevel = getReportLevel(plan, parentLocation, parentIdentifierString);

    ReportTypeEnum finalReportTypeEnum = SURVEY;

    Map<UUID, RowData> rowDataMap = new HashMap<>();

    List<RowData> rowData = getRowData(parentLocation, finalReportTypeEnum, plan, null,
        reportLevel, null,
        parentIdentifierString, null, locationDetails, null);

    if (rowData != null) {
      rowDataMap = rowData.stream()
          .collect(Collectors.toMap(RowData::getLocationIdentifier, row -> row, (a, b) -> b));
    }

    return rowData;
  }

  @Data
  @AllArgsConstructor
  public static class GhaLocation implements Serializable {

    private String identifier;
    private String name;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof GhaLocation)) {
        return false;
      }
      GhaLocation that = (GhaLocation) o;
      return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getIdentifier());
    }
  }

  private List<RowData> getRowData(Location parentLocation, ReportTypeEnum reportTypeEnum,
      Plan plan,
      PlanLocationDetails loc, String reportLevel, List<String> filters,
      String parentIdentifierString, MdaLiteReportType type,
      List<PlanLocationDetails> locationDetails, String clickedColumn) {

    List<AmdrPerformanceDataProjection> totalIndexVerifiedForRoot = amdrRepository.getTotalIndexVerifiedForRoot(
        parentLocation == null ? "null" : parentLocation.getIdentifier().toString());

    Map<String, AmdrPerformanceDataProjection> verifiedMap = totalIndexVerifiedForRoot.stream()
        .filter(verified -> verified.getVerified() != null)
        .filter(verified -> verified.getVerified().equals("yes"))
        .map(verified -> new SimpleEntry<>(verified.getLocationIdentifier(), verified))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b));

//    Map<String, Long> totalCountMap = totalIndexVerifiedForRoot.stream()
//        .collect(Collectors.groupingBy(AmdrPerformanceDataProjection::getLocationIdentifier,
//            Collectors.summingLong(AmdrPerformanceDataProjection::getCount)));

    List<GhaLocation> indexesVerified = totalIndexVerifiedForRoot.stream()
        .filter(verified -> verified.getVerified() != null)
        .filter(verified -> verified.getVerified().equals("yes"))
        .map(verified -> new GhaLocation(verified.getLocationIdentifier(),
            verified.getLocationName()))
        .collect(Collectors.toList());

    List<AmdrPerformanceDataProjection> totalIndexToBeVerifiedForRoot = amdrRepository.getTotalIndexToBeVerifiedForRoot(
        parentLocation == null ? "null" : parentLocation.getIdentifier().toString());

    List<GhaLocation> indexesToBeVerified = totalIndexToBeVerifiedForRoot.stream()
        .map(toBeVerified -> new GhaLocation(toBeVerified.getLocationIdentifier(),
            toBeVerified.getLocationName()))
        .collect(Collectors.toList());

    Map<String, AmdrPerformanceDataProjection> toBeVerifiedMap = totalIndexToBeVerifiedForRoot.stream()
        .map(toBeVerified -> new SimpleEntry<>(toBeVerified.getLocationIdentifier(), toBeVerified))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b));

    List<AmdrAggregatedDataProjection> racdPerformanceCounts = amdrRepository.getRACDPerformanceCounts(
        parentLocation == null ? "null" : parentLocation.getIdentifier().toString());

    Map<String, AmdrPerformanceAggregatedData> visitedCounts = racdPerformanceCounts.stream()
        .map(item -> {
          Map<String, Long> stateMap = new HashMap<>();
          try {
            stateMap = objectMapper.readValue(
                item.getObj(),
                new TypeReference<>() {
                });
          } catch (Exception e) {
            stateMap = null;
          }
          return AmdrPerformanceAggregatedData.builder()
              .locationIdentifier(item.getLocationIdentifier())
              .locationName(item.getLocationName())
              .parentName(item.getParentName())
              .parentIdentifier(item.getParentIdentifier())
              .stateCounts(stateMap)
              .build();
        }).collect(
            Collectors.toMap(AmdrPerformanceAggregatedData::getLocationIdentifier, item -> item,
                (a, b) -> b));

    List<AmdrAggregatedDataProjection> performanceStartEndTimesProjection = amdrRepository.getPerformanceStartEndTimes(
        parentLocation == null ? "null" : parentLocation.getIdentifier().toString());

    Map<String, AmdrPerformanceAggregatedDataString> performanceStartEndTimesMap = performanceStartEndTimesProjection.stream()
        .map(item -> {
          Map<String, String> stateMap = new HashMap<>();
          try {
            stateMap = objectMapper.readValue(
                item.getObj(),
                new TypeReference<>() {
                });
          } catch (Exception e) {
            stateMap = null;
          }
          return AmdrPerformanceAggregatedDataString.builder()
              .locationIdentifier(item.getLocationIdentifier())
              .locationName(item.getLocationName())
              .parentName(item.getParentName())
              .parentIdentifier(item.getParentIdentifier())
              .stateCounts(stateMap)
              .build();
        }).collect(Collectors.toMap(AmdrPerformanceAggregatedDataString::getLocationIdentifier,
            item -> item, (a, b) -> b));

    List<AmdrAggregatedDataProjection> withinThreeDayProjection = amdrRepository.getWithinThreeDayCounts(
        parentLocation == null ? "null" : parentLocation.getIdentifier().toString());

    Map<String, AmdrPerformanceAggregatedData> withinThreeDay = withinThreeDayProjection.stream()
        .map(item -> {
          Map<String, Long> stateMap = new HashMap<>();
          try {
            stateMap = objectMapper.readValue(
                item.getObj(),
                new TypeReference<>() {
                });
          } catch (Exception e) {
            stateMap = null;
          }
          return AmdrPerformanceAggregatedData.builder()
              .locationIdentifier(item.getLocationIdentifier())
              .locationName(item.getLocationName())
              .parentName(item.getParentName())
              .parentIdentifier(item.getParentIdentifier())
              .stateCounts(stateMap)
              .build();
        }).collect(Collectors.toMap(AmdrPerformanceAggregatedData::getLocationIdentifier,
            item -> item, (a, b) -> b));

    Set<GhaLocation> all = new HashSet<>();
    all.addAll(indexesVerified);
    all.addAll(indexesToBeVerified);

    List<RowData> rowDataList = all.stream().map(ghaLocation -> {
      RowData rowData = new RowData();

      AmdrPerformanceDataProjection verifiedProjection = verifiedMap.get(
          ghaLocation.getIdentifier());

      AmdrPerformanceDataProjection toBeVerifiedProjection = toBeVerifiedMap.get(
          ghaLocation.getIdentifier());

      AmdrPerformanceAggregatedData amdrRACDVisitedsCounts = visitedCounts.get(
          ghaLocation.getIdentifier());

      AmdrPerformanceAggregatedDataString performanceStartEndTimes = performanceStartEndTimesMap.get(
          ghaLocation.getIdentifier());

      long totalVerified = verifiedProjection != null ? verifiedProjection.getCount() : 0L;

      long totalToBeVerified =
          toBeVerifiedProjection != null ? toBeVerifiedProjection.getCount() : 0L;

//      Long aLong = totalCountMap.get(ghaLocation.getIdentifier());

      long totalCases = totalVerified + totalToBeVerified;

      AmdrPerformanceAggregatedData withInThreeDayData = withinThreeDay.get(
          ghaLocation.getIdentifier());

      Long visitedRACD = 0l;
      Long toBeVisitedRACD = 0l;
      if (amdrRACDVisitedsCounts != null && amdrRACDVisitedsCounts.getStateCounts() != null) {
        if (amdrRACDVisitedsCounts.getStateCounts().containsKey("visited")) {
          visitedRACD = amdrRACDVisitedsCounts.getStateCounts().get("visited");
        }
        if (amdrRACDVisitedsCounts.getStateCounts().containsKey("notVisited")) {
          toBeVisitedRACD = amdrRACDVisitedsCounts.getStateCounts().get("notVisited");
        }
      }

      long totalRcdCases = visitedRACD  + toBeVisitedRACD;



      String start = "";
      String end = "";
      if (performanceStartEndTimes != null && performanceStartEndTimes.getStateCounts() != null) {
        if (performanceStartEndTimes.getStateCounts().containsKey("start")) {
          start = performanceStartEndTimes.getStateCounts().get("start");
        }
        if (performanceStartEndTimes.getStateCounts().containsKey("end")) {
          end = performanceStartEndTimes.getStateCounts().get("end");
        }
      }

      Long rcd3Day = 0l;
      Long rcdAllDay = 0l;
      Long index3Day = 0l;
      Long indexAllDay = 0l;
      if (withInThreeDayData != null && withInThreeDayData.getStateCounts() != null) {
        if (withInThreeDayData.getStateCounts().containsKey("rcd-3")) {
          rcd3Day = withInThreeDayData.getStateCounts().get("rcd-3");
        }
        if (withInThreeDayData.getStateCounts().containsKey("rcd-all")) {
          rcdAllDay = withInThreeDayData.getStateCounts().get("rcd-all");
        }

        if (withInThreeDayData.getStateCounts().containsKey("index_case_member-3")) {
          index3Day = withInThreeDayData.getStateCounts().get("index_case_member-3");
        }
        if (withInThreeDayData.getStateCounts().containsKey("index_case_member-all")) {
          indexAllDay = withInThreeDayData.getStateCounts().get("index_case_member-all");
        }
      }



      Map<String, ColumnData> columnDataMap = new LinkedHashMap<>();
      ColumnData verifiedColumnData = ColumnData.builder()
          .value(totalVerified)
          .build();
      columnDataMap.put("Count: Total # index cases verified", verifiedColumnData);

      ColumnData toBeVerifiedColumnData = ColumnData.builder()
          .value(totalToBeVerified)
          .build();
      columnDataMap.put("Count: Total # index cases to be verified", toBeVerifiedColumnData);

      ColumnData indexCaseWith3DaysColumnData = ColumnData.builder()
          .value(index3Day)
          .build();
      columnDataMap.put("Count: Total # index cases within 3 days", indexCaseWith3DaysColumnData);

      ColumnData indexCaseWithAllColumnData = ColumnData.builder()
          .value(indexAllDay)
          .build();
      columnDataMap.put("Count: Total # index cases", indexCaseWithAllColumnData);

      ColumnData visitedRACDData = ColumnData.builder()
          .value(visitedRACD)
          .build();
      columnDataMap.put("Count: # structures visited for RACD", visitedRACDData);

      ColumnData toBeVisitedData = ColumnData.builder()
          .value(toBeVisitedRACD)
          .build();
      columnDataMap.put("Count: # remaining structures to be visited for RACD", toBeVisitedData);

      ColumnData rcdWith3DaysColumnData = ColumnData.builder()
          .value(rcd3Day)
          .build();
      columnDataMap.put("Count: Total # index cases within 3 days", rcdWith3DaysColumnData);

      ColumnData rcdWithAllColumnData = ColumnData.builder()
          .value(rcdAllDay)
          .build();
      columnDataMap.put("Count: Total # rcd cases", rcdWithAllColumnData);

      ColumnData startColumnData = ColumnData.builder()
          .value(start)
          .build();
      columnDataMap.put("Daily average: Start time", startColumnData);

      ColumnData endColumnData = ColumnData.builder()
          .value(end)
          .build();
      columnDataMap.put("Daily average: End time", endColumnData);

      rowData.setColumnDataMap(columnDataMap);
      rowData.setUserId(UUID.fromString(ghaLocation.getIdentifier()).toString());
      rowData.setUserLabel("Location");
      rowData.setUserName(ghaLocation.getName());

      return rowData;
    }).collect(Collectors.toList());

    return rowDataList;
  }

  private String getReportLevel(Plan plan, Location parentLocation, String parentIdentifierString) {

    String parentOfGeoLevelDirectlyAboveStructure = null;
    if (plan.getLocationHierarchy().getNodeOrder().contains(LocationConstants.STRUCTURE)) {
      parentOfGeoLevelDirectlyAboveStructure = plan.getLocationHierarchy().getNodeOrder().get(
          plan.getLocationHierarchy().getNodeOrder().indexOf(LocationConstants.STRUCTURE) - 2);
    }

    String planTarget = null;
    String directlyAbovePlanTarget = null;

    if (plan.getLocationHierarchy().getNodeOrder()
        .contains(plan.getPlanTargetType().getGeographicLevel().getName())) {
      planTarget = plan.getLocationHierarchy().getNodeOrder().get(
          plan.getLocationHierarchy().getNodeOrder()
              .indexOf(plan.getPlanTargetType().getGeographicLevel().getName()));
    }

    if (plan.getLocationHierarchy().getNodeOrder()
        .contains(plan.getPlanTargetType().getGeographicLevel().getName())) {
      directlyAbovePlanTarget = plan.getLocationHierarchy().getNodeOrder().get(
          plan.getLocationHierarchy().getNodeOrder()
              .indexOf(plan.getPlanTargetType().getGeographicLevel().getName()) - 1);
    }

    String geoLevelDirectlyAboveStructure = null;
    if (plan.getLocationHierarchy().getNodeOrder().contains(LocationConstants.STRUCTURE)) {
      geoLevelDirectlyAboveStructure = plan.getLocationHierarchy().getNodeOrder().get(
          plan.getLocationHierarchy().getNodeOrder().indexOf(LocationConstants.STRUCTURE) - 1);
    }

    String geoLevelDirectlyAbovePlanTarget = null;
    if (plan.getLocationHierarchy().getNodeOrder()
        .contains(plan.getPlanTargetType().getGeographicLevel().getName())) {
      geoLevelDirectlyAbovePlanTarget = plan.getLocationHierarchy().getNodeOrder().get(
          plan.getLocationHierarchy().getNodeOrder()
              .indexOf(plan.getPlanTargetType().getGeographicLevel().getName()) - 1);
    }

    boolean containsStructure = plan.getLocationHierarchy().getNodeOrder()
        .contains(LocationConstants.STRUCTURE);

    String lowestLevel = plan.getLocationHierarchy().getNodeOrder()
        .get(plan.getLocationHierarchy().getNodeOrder().size() - 1);

    String secondHighestLevel = plan.getLocationHierarchy().getNodeOrder()
        .get(1);

    if (parentIdentifierString != null && parentIdentifierString.contains("SUPERVISOR")) {
      return SUPERVISOR_LEVEL;
    } else if (parentIdentifierString != null && parentIdentifierString.contains("CDD")) {
      return CDD_LEVEL;
    } else {
      if (parentLocation == null) {
        return ALL_OTHER_LEVELS;
      } else {
        if (containsStructure) {
          if (parentLocation.getGeographicLevel().getName().equals(LocationConstants.STRUCTURE)) {
            return WITHIN_STRUCTURE_LEVEL;
          } else if (geoLevelDirectlyAboveStructure != null) {
            String reportLevel = ALL_OTHER_LEVELS;

            if (parentLocation.getGeographicLevel().getName()
                .equals(geoLevelDirectlyAboveStructure)) {
              reportLevel = STRUCTURE_LEVEL;
            }
            if (parentLocation.getGeographicLevel().getName()
                .equals(parentOfGeoLevelDirectlyAboveStructure)) {
              if (parentLocation.getGeographicLevel().getName()
                  .equals(geoLevelDirectlyAbovePlanTarget)) {
                reportLevel = IS_ON_PLAN_TARGET;
              } else {
                reportLevel = DIRECTLY_ABOVE_STRUCTURE_LEVEL;
              }
            }
            return reportLevel;
          } else {
            if (parentLocation.getGeographicLevel().getName()
                .equals(secondHighestLevel)) {
              return ALL_OTHER_LEVELS;
            } else {
              return ALL_OTHER_LEVELS;
            }
          }
        } else {
          if (parentLocation.getGeographicLevel().getName().equals(directlyAbovePlanTarget)) {
            return IS_ON_PLAN_TARGET;
          } else {
            if (parentLocation.getGeographicLevel().getName().equals(lowestLevel)) {
              return LOWEST_LITE_TOUCH_LEVEL;
            } else {

              return ALL_OTHER_LEVELS;
            }
          }
        }
      }
    }
  }

  public List<PlanLocationDetails> getPlanLocationDetails(UUID planIdentifier,
      UUID parentIdentifier, Plan plan, Location parentLocation) {
    List<PlanLocationDetails> locationDetails = new ArrayList<>();
    if (parentLocation == null ||
        !parentLocation.getGeographicLevel().getName().equals(LocationConstants.STRUCTURE)) {

      if (parentIdentifier == null) {
        locationDetails.addAll(locationService.getRootLocationsByPlanIdentifier(planIdentifier));
      } else {

        int structureNodeIndex = plan.getLocationHierarchy().getNodeOrder()
            .indexOf(LocationConstants.STRUCTURE);
        int locationNodeIndex = plan.getLocationHierarchy().getNodeOrder()
            .indexOf(parentLocation.getGeographicLevel().getName());
        if (structureNodeIndex < 0) {
          locationDetails = locationService.getAssignedLocationsByParentIdentifierAndPlanIdentifier(
              parentIdentifier, planIdentifier, false);
        } else if (locationNodeIndex + 1 < structureNodeIndex) {
          locationDetails = locationService.getAssignedLocationsByParentIdentifierAndPlanIdentifier(
              parentIdentifier, planIdentifier, (locationNodeIndex + 2) == structureNodeIndex);
        } else {

          locationDetails = locationService.getLocationsByParentIdentifierAndPlanIdentifier(
              parentIdentifier, planIdentifier);
        }
      }
    } else {
      PlanLocationDetails planLocations = new PlanLocationDetails();
      planLocations.setParentLocation(parentLocation);
      planLocations.setLocation(parentLocation);
      planLocations.setHasChildren(false);
      planLocations.setAssignedLocations(0L);
      planLocations.setChildrenNumber(0L);
      planLocations.setAssignedTeams(0L);
      locationDetails.add(planLocations);
    }
    return locationDetails;
  }
}
