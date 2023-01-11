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
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SurveyDashboardService {

  private final PlanLocationsService planLocationsService;
  private final DashboardProperties dashboardProperties;
  private final LocationBusinessStatusService locationBusinessStatusService;

  private static final String TOTAL_STRUCTURES = "Total structures";
  private static final String TOTAL_STRUCTURES_TARGETED = "Total Structures Targeted";
  private static final String TOTAL_STRUCTURES_VISITED = "Total structures visited";
  private static final String TOTAL_STRUCTURES_MDA_COMPLETE = "Total Structures MDA Complete";
  private static final String STRUCTURE_STATUS = "Structure Status";
  public static final String VISITATION_COVERAGE = "Visitation Coverage (Visited/Target)";
  public static final String DISTRIBUTION_COVERAGE = "Distribution Coverage (MDA Completed/Visited)";

  public List<RowData> getIRSFullData(Plan plan, Location childLocation) {

    Map<String, ColumnData> columns = new LinkedHashMap<>();
    columns.put(TOTAL_STRUCTURES, getTotalStructuresCounts(plan, childLocation));
    columns.put(TOTAL_STRUCTURES_TARGETED, getTotalStructuresTargetedCount(plan, childLocation));
    columns.put(
        TOTAL_STRUCTURES_VISITED, getTotalStructuresFoundCount(plan, childLocation));
    columns.put(TOTAL_STRUCTURES_MDA_COMPLETE, getTotalStructuresMdaComplete(plan, childLocation));
    columns.put(VISITATION_COVERAGE, getFoundCoverage(plan, childLocation));
    columns.put(DISTRIBUTION_COVERAGE,getDistributionCoverage(plan, childLocation));
    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  public List<RowData> getIRSFullCoverageStructureLevelData(Plan plan, Location childLocation) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(STRUCTURE_STATUS,
        getLocationBusinessState(plan,childLocation));
    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  private ColumnData getLocationBusinessState(Plan plan,Location location){
    TaskBusinessStateTracker locationBusinessState = locationBusinessStatusService.findLocationBusinessState(
        plan.getLocationHierarchy().getIdentifier(), location.getIdentifier(),
        plan.getIdentifier());

    ColumnData columnData = DashboardUtils.getStringValueColumnData();

    if (locationBusinessState!=null){
      columnData.setValue( locationBusinessState.getTaskBusinessStatus());
    }

    return columnData;
  }

  private ColumnData getFoundCoverage(Plan plan, Location childLocation) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double foundStructures = (double) getTotalStructuresFoundCount(plan, childLocation).getValue();
    double targetedStructures = (double) getTotalStructuresTargetedCount(plan,
        childLocation).getValue();
    if (targetedStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((foundStructures / targetedStructures) * 100);
    }
    columnData.setMeta("Visited Structures: "+foundStructures+ " / " + "Targeted Structure: "+targetedStructures);
    return columnData;
  }

  private ColumnData getDistributionCoverage(Plan plan, Location childLocation) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double foundStructures = (double) getTotalStructuresFoundCount(plan, childLocation).getValue();
    double mdaComplete = (double) getTotalStructuresMdaComplete(plan, childLocation).getValue();
    if (foundStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((mdaComplete / foundStructures) * 100);
    }
    columnData.setMeta("MDA Complete: "+mdaComplete+ " / " + "Visited: "+foundStructures);
    return columnData;
  }

  private ColumnData getTotalStructuresMdaComplete(Plan plan, Location childLocation) {

    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);

    Long completedStructuresCountObj = null;
    LocationBusinessStateCount completedStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.MDA_COMPLETE, plan.getLocationHierarchy().getIdentifier());
    if (completedStructuresCountObjCount != null) {
      completedStructuresCountObj = completedStructuresCountObjCount.getLocationCount();
    }

    double completedStructuresCount = 0;
    if (completedStructuresCountObj != null) {
      completedStructuresCount = completedStructuresCountObj;
    }

    columnData.setValue(completedStructuresCount);

    return columnData;
  }


  private ColumnData getTotalStructuresCounts(Plan plan, Location childLocation) {

    Long totalStructuresCountObj = locationBusinessStatusService.getLocationCountsForGeoLevelByHierarchyLocationParent(
        childLocation.getIdentifier(), plan.getLocationHierarchy().getIdentifier(),
        LocationConstants.STRUCTURE, plan);

    double totalStructuresCount = 0;
    if (totalStructuresCountObj != null) {
      totalStructuresCount = totalStructuresCountObj;
    }

    Long notEligibleStructuresCountObj = null;
    LocationBusinessStateCount notEligibleStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.NOT_ELIGIBLE, plan.getLocationHierarchy().getIdentifier());

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

  private ColumnData getTotalStructuresTargetedCount(Plan plan, Location childLocation) {


    Long totalStructuresTargetedCountObj = planLocationsService.getAssignedStructureCountByLocationParentAndPlan(
        plan, childLocation);


    double totalStructuresInPlanLocationCount = 0;
    if (totalStructuresTargetedCountObj != null) {
      totalStructuresInPlanLocationCount = totalStructuresTargetedCountObj;
    }

    Long notEligibleStructuresCountObj = null;
    LocationBusinessStateCount notEligibleStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.NOT_ELIGIBLE, plan.getLocationHierarchy().getIdentifier());

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

  private ColumnData getTotalStructuresFoundCount(Plan plan, Location childLocation) {

    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);

    Long totalStructuresTargetedCountObj = planLocationsService.getAssignedStructureCountByLocationParentAndPlan(
        plan, childLocation);


    double totalStructuresInPlanLocationCount = 0;
    if (totalStructuresTargetedCountObj != null) {
      totalStructuresInPlanLocationCount = totalStructuresTargetedCountObj;
    }

    Long notEligibleStructuresCountObj = null;
    LocationBusinessStateCount notEligibleStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.NOT_ELIGIBLE, plan.getLocationHierarchy().getIdentifier());

    if (notEligibleStructuresCountObjCount != null) {
      notEligibleStructuresCountObj = notEligibleStructuresCountObjCount.getLocationCount();
    }

    double notEligibleStructuresCount = 0;
    if (notEligibleStructuresCountObj != null) {
      notEligibleStructuresCount = notEligibleStructuresCountObj;
    }

    double totalStructuresInTargetedCount =
        totalStructuresInPlanLocationCount - notEligibleStructuresCount;

    Long notVisitedStructuresCountObj = null;
    LocationBusinessStateCount notVisitedStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.NOT_VISITED, plan.getLocationHierarchy().getIdentifier());

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
            businessStatus==null?"No State":businessStatus);
        loc.getProperties().setStatusColor(getBusinessStatusColor(businessStatus));
      }

    }).collect(Collectors.toList());
  }
}


