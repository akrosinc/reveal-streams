package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.util.DashboardUtils.getBusinessStatusColor;

import com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.ReportTypeEnum;
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
public class LsmDashboardService {

  private final PlanLocationsService planLocationsService;
  private final DashboardProperties dashboardProperties;
  private final LocationBusinessStatusService locationBusinessStatusService;

  private static final String TOTAL_WATER_BODIES = "No of Water Bodies";
  private static final String TOTAL_STRUCTURES = "No of Structures";
  private static final String TOTAL_WATER_BODIES_TARGETED = "Total Water Bodies Targeted";
  private static final String TOTAL_STRUCTURES_TARGETED = "Total Structures Targeted";
  private static final String TOTAL_WATER_BODIES_SURVEYED = "Total Water Bodies Surveyed";
  private static final String TOTAL_STRUCTURES_SURVEYED = "Total Structures Surveyed";
  private static final String STRUCTURE_STATUS = "Structure Status";
  public static final String SURVEY_COVERAGE = "Survey Coverage (Surveyed/Total)";

  public List<RowData> getIRSFullData(Plan plan, Location childLocation,
      ReportTypeEnum reportTypeEnum) {

    Map<String, ColumnData> columns = new LinkedHashMap<>();
    if (reportTypeEnum.equals(ReportTypeEnum.LSM_HABITAT_SURVEY)) {
      columns.put(TOTAL_WATER_BODIES, getTotalWaterBodyCounts(plan, childLocation));
    } else {
      columns.put(TOTAL_STRUCTURES, getTotalStructuresCounts(plan, childLocation));
    }

    if (reportTypeEnum.equals(ReportTypeEnum.LSM_HABITAT_SURVEY)) {
      columns.put(TOTAL_WATER_BODIES_TARGETED, getTotalStructuresTargetedCountForWaterBodies(plan, childLocation));
    } else {
      columns.put(TOTAL_STRUCTURES_TARGETED, getTotalStructuresTargetedCount(plan, childLocation));
    }

    if (reportTypeEnum.equals(ReportTypeEnum.LSM_HABITAT_SURVEY)) {
      columns.put(TOTAL_WATER_BODIES_SURVEYED, getTotalWaterBodiesSurveyed(plan, childLocation));
    } else {
      columns.put(TOTAL_STRUCTURES_SURVEYED, getTotalStructuresSurveyed(plan, childLocation));
    }

    if (reportTypeEnum.equals(ReportTypeEnum.LSM_HABITAT_SURVEY)) {
      columns.put(SURVEY_COVERAGE,getWaterBodyDistributionCoverage(plan, childLocation));
    } else {
      columns.put(SURVEY_COVERAGE,getDistributionCoverage(plan, childLocation));
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
    } else {
      columnData.setValue(BusinessStatus.NOT_VISITED);
    }

    return columnData;
  }


  private ColumnData getDistributionCoverage(Plan plan, Location childLocation) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double surveyed = (double) getTotalStructuresSurveyed(plan, childLocation).getValue();
    double targeted = (double) getTotalStructuresTargetedCount(plan, childLocation).getValue();
    if (targeted == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((surveyed / targeted) * 100);
    }
    columnData.setMeta("Surveyed structures: "+surveyed+ " / " + "Total Structures: "+targeted);
    return columnData;
  }

  private ColumnData getWaterBodyDistributionCoverage(Plan plan, Location childLocation) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double surveyed = (double) getTotalWaterBodiesSurveyed(plan, childLocation).getValue();
    double targeted = (double) getTotalStructuresTargetedCountForWaterBodies(plan, childLocation).getValue();
    if (targeted == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((surveyed / targeted) * 100);
    }
    columnData.setMeta("Surveyed water bodies: "+surveyed+ " / " + "Total water bodies: "+targeted);
    return columnData;
  }

  private ColumnData getTotalStructuresSurveyed(Plan plan, Location childLocation) {

    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);

    Long completedStructuresCountObj = null;
    LocationBusinessStateCount completedStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForNonWaterBodies(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.COMPLETE, plan.getLocationHierarchy().getIdentifier());
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
  private ColumnData getTotalWaterBodiesSurveyed(Plan plan, Location childLocation) {

    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);

    Long completedStructuresCountObj = null;
    LocationBusinessStateCount completedStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForWaterBodies(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.COMPLETE, plan.getLocationHierarchy().getIdentifier());
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

    Long totalStructuresCountObj = locationBusinessStatusService.getLocationCountsForGeoLevelByHierarchyLocationParentForNonWaterBodies(
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

  private ColumnData getTotalWaterBodyCounts(Plan plan, Location childLocation) {

    Long totalStructuresCountObj = locationBusinessStatusService.getLocationCountsForGeoLevelByHierarchyLocationParentForWaterBodies(
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


    Long totalStructuresTargetedCountObj = planLocationsService.getAssignedStructureCountByLocationParentAndPlanForNonWaterBodies(
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

  private ColumnData getTotalStructuresTargetedCountForWaterBodies(Plan plan, Location childLocation) {


    Long totalStructuresTargetedCountObj = planLocationsService.getAssignedStructureCountByLocationParentAndPlanForWaterBodies(
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
        dashboardProperties.getLsmSurveyDefaultDisplayColumns().getOrDefault(reportLevel, null));
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }


  private List<LocationResponse> setGeoJsonProperties(Map<UUID, RowData> rowDataMap,
      List<LocationResponse> locationResponses) {
    return locationResponses.stream().peek(loc -> {
      loc.getProperties().setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
      loc.getProperties().setId(loc.getIdentifier().toString());
      if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(SURVEY_COVERAGE)
          != null) {
        loc.getProperties().setSurveyCoverage(
            rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(SURVEY_COVERAGE)
                .getValue());
      }
      if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
          .get(STRUCTURE_STATUS) != null) {
        String businessStatus = (String) rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
            .get(STRUCTURE_STATUS).getValue();
        loc.getProperties().setBusinessStatus(
            businessStatus);
        loc.getProperties().setStatusColor(getBusinessStatusColor(businessStatus));
      }

    }).collect(Collectors.toList());
  }
}


