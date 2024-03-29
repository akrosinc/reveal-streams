package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.util.DashboardUtils.getBusinessStatusColor;

import com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.factory.LocationResponseFactory;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.domain.TaskBusinessStateTracker;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.service.LocationBusinessStatusService;
import com.revealprecision.revealstreams.service.PlanLocationsService;
import com.revealprecision.revealstreams.util.DashboardUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class SurveyDashboardService {

  private final PlanLocationsService planLocationsService;
  private final DashboardProperties dashboardProperties;
  private final LocationBusinessStatusService locationBusinessStatusService;

  private static final String TOTAL_STRUCTURES = "Total structures";
  private static final String TOTAL_STRUCTURES_TARGETED = "Total Structures Targeted";
  private static final String TOTAL_STRUCTURES_VISITED = "Total structures visited";
  private static final String TOTAL_STRUCTURES_MDA_COMPLETE_OR_PARTIALLY_COMPLETE = "Total Structures MDA Complete or Partially complete MDA";
  private static final String STRUCTURE_STATUS = "Structure Status";
  public static final String VISITATION_COVERAGE = "Visitation Coverage (Visited/Target)";
  public static final String DISTRIBUTION_COVERAGE = "Distribution Coverage (MDA Completed/Visited)";

  public List<RowData> getIRSFullData(Plan plan, Location childLocation) {

    Map<String, ColumnData> columns = new LinkedHashMap<>();

    Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap = locationBusinessStatusService.getLocationBusinessStateObjPerGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(),
        childLocation.getGeographicLevel().getName(), plan.getLocationHierarchy().getIdentifier());

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

    columns.put(TOTAL_STRUCTURES,
        getTotalStructuresCounts(totalStructuresCountObj, locationBusinessStateObjPerGeoLevelMap));
    columns.put(TOTAL_STRUCTURES_TARGETED,
        getTotalStructuresTargetedCount(totalStructuresTargetedCountObj,
            locationBusinessStateObjPerGeoLevelMap));
    columns.put(
        TOTAL_STRUCTURES_VISITED,
        getTotalStructuresFoundCount(totalStructuresTargetedCountObj,
            locationBusinessStateObjPerGeoLevelMap));
    columns.put(TOTAL_STRUCTURES_MDA_COMPLETE_OR_PARTIALLY_COMPLETE,
        getTotalStructuresMdaCompleteOrPartiallyCompleted(
            locationBusinessStateObjPerGeoLevelMap));
    columns.put(VISITATION_COVERAGE,
        getFoundCoverage(totalStructuresTargetedCountObj, locationBusinessStateObjPerGeoLevelMap));
    columns.put(DISTRIBUTION_COVERAGE,
        getDistributionCoverage(totalStructuresTargetedCountObj, locationBusinessStateObjPerGeoLevelMap));
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

  private ColumnData getDistributionCoverage(long totalStructuresTargetedCountObj,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double foundStructures = (double) getTotalStructuresFoundCount(totalStructuresTargetedCountObj,
        locationBusinessStateObjPerGeoLevelMap).getValue();
    double mdaComplete = (double) getTotalStructuresMdaCompleteOrPartiallyCompleted(
        locationBusinessStateObjPerGeoLevelMap).getValue();
    if (foundStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((mdaComplete / foundStructures) * 100);
    }
    columnData.setMeta("MDA Complete: " + mdaComplete + " / " + "Visited: " + foundStructures);
    return columnData;
  }

  private ColumnData getTotalStructuresMdaCompleteOrPartiallyCompleted(
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {

    ColumnData columnData = new ColumnData();

    double completedStructuresCount;
    LocationBusinessStateCount completedStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.MDA_COMPLETE);
    if (completedStructuresCountObjCount != null) {
      completedStructuresCount = completedStructuresCountObjCount.getLocationCount();
    } else {
      completedStructuresCount = 0L;
    }

    double partiallyCompletedStructuresCount;
    LocationBusinessStateCount partiallyCompletedStructuresCountObjCount = locationBusinessStateObjPerGeoLevelMap.get(
        BusinessStatus.PARTIALLY_COMPLETE);
    if (partiallyCompletedStructuresCountObjCount != null) {
      partiallyCompletedStructuresCount = partiallyCompletedStructuresCountObjCount.getLocationCount();
    } else {
      partiallyCompletedStructuresCount = 0;
    }

    double partiallyCompleteOrComplete =
        partiallyCompletedStructuresCount + completedStructuresCount;

    columnData.setValue(partiallyCompleteOrComplete);

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


  public FeatureSetResponse getFeatureSetResponse(UUID parentIdentifier,
      List<PlanLocationDetails> locationDetails, Map<UUID, RowData> rowDataMap,
      String reportLevel) {
    FeatureSetResponse response = new FeatureSetResponse();
    response.setType("FeatureCollection");
    List<LocationResponse> locationResponses = locationDetails.stream()
        .map(loc -> LocationResponseFactory.fromPlanLocationDetails(loc, parentIdentifier))
        .collect(Collectors.toList());

    locationResponses = setGeoJsonProperties(rowDataMap, locationResponses);
    response.setDefaultDisplayColumn(
        dashboardProperties.getSurveyDefaultDisplayColumns().getOrDefault(reportLevel, null));
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }


  private List<LocationResponse> setGeoJsonProperties(Map<UUID, RowData> rowDataMap,
      List<LocationResponse> locationResponses) {
    return locationResponses.stream().peek(loc -> {
      loc.getProperties().setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
      loc.getProperties().setId(loc.getIdentifier().toString());
      if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(VISITATION_COVERAGE)
          != null) {
        loc.getProperties().setFoundCoverage(
            rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(VISITATION_COVERAGE)
                .getValue());
      }
      if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
          .get(STRUCTURE_STATUS) != null) {
        String businessStatus = (String) rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
            .get(STRUCTURE_STATUS).getValue();
        loc.getProperties().setBusinessStatus(
            businessStatus == null ? "No State" : businessStatus);
        loc.getProperties().setStatusColor(getBusinessStatusColor(businessStatus));
      }

    }).collect(Collectors.toList());
  }
}


