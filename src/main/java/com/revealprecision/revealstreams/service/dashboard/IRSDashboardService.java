package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.props.DashboardProperties.SPRAY_COVERAGE_OF_TARGETED;
import static com.revealprecision.revealstreams.util.DashboardUtils.getBusinessStatusColor;
import static com.revealprecision.revealstreams.util.DashboardUtils.getGeoNameDirectlyAboveStructure;
import static com.revealprecision.revealstreams.util.DashboardUtils.getLocationBusinessState;
import static com.revealprecision.revealstreams.util.DashboardUtils.getStringValueColumnData;

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
import com.revealprecision.revealstreams.persistence.domain.Report;
import com.revealprecision.revealstreams.persistence.domain.ReportIndicators;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import com.revealprecision.revealstreams.persistence.repository.ReportRepository;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.service.LocationBusinessStatusService;
import com.revealprecision.revealstreams.service.PlanLocationsService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IRSDashboardService {

  public static final String NOT_SPRAYED_REASON = "Not Sprayed Reason";
  public static final String PHONE_NUMBER = "Phone Number";
  public static final String N_A = "n/a";
  public static final String HEAD_OF_HOUSEHOLD = "Head of Household";
  private final PlanLocationsService planLocationsService;
  private final DashboardProperties dashboardProperties;
  private final LocationBusinessStatusService locationBusinessStatusService;

  private static final String TOTAL_SPRAY_AREAS = "Total spray areas";
  private static final String TARGET_SPRAY_AREAS = "Targeted spray areas";
  private static final String VISITED_AREAS = "Total  Spray Areas Visited";
  private static final String TOTAL_STRUCTURES = "Total structures";
  private static final String TOTAL_STRUCTURES_TARGETED = "Total Structures Targeted";
  private static final String TOTAL_STRUCTURES_FOUND = "Total Structures Found";
  private static final String STRUCTURES_SPRAYED = "Total Structures Sprayed";
  private static final String PERCENTAGE_VISITED_EFFECTIVELY = "Spray Areas Effectively sprayed";
  private static final String STRUCTURE_STATUS = "Structure Status";
  private static final String NO_OF_ROOMS = "No of Rooms";
  private static final String NO_OF_MALES = "No of Males";
  private static final String NO_OF_FEMALES = "No of Females";
  private static final String NO_OF_PREGNANT_WOMEN = "No of Pregnant Women";
  private static final String FOUND_COVERAGE = "Found Coverage (Found/Target)";
  private static final String SPRAY_COVERAGE_OF_FOUND_STRUCTURES = "Spray Coverage of Found(Sprayed/Found)";
  private static final String STRUCTURES_REMAINING_TO_SPRAY_TO_REACH_90 = "Structures remaining to spray to reach 90% spray coverage";
  private static final String REVIEWED_WITH_DECISION = "Reviewed with decision";
  private static final String MOBILIZED = "Mobilized";

  boolean isDatastoresInitialized = false;
  private final ReportRepository planReportRepository;

  public List<RowData> getIRSFullData(Plan plan, Location childLocation) {
    Report reportEntry = planReportRepository.findByPlanAndLocation(plan, childLocation)
        .orElse(null);
    Map<String, ColumnData> columns = new LinkedHashMap<>();
    columns.put(TOTAL_SPRAY_AREAS,
        getTotalAreas(plan, childLocation, getGeoNameDirectlyAboveStructure(plan)));
    columns.put(TARGET_SPRAY_AREAS, getTargetedAreas(plan, childLocation));
    columns.put(VISITED_AREAS, getOperationalAreaVisitedCounts(plan, childLocation));
    columns.put(PERCENTAGE_VISITED_EFFECTIVELY, getSprayedEffectively(plan, childLocation));
    columns.put(TOTAL_STRUCTURES, getTotalStructuresCounts(plan, childLocation, reportEntry));
    columns.put(TOTAL_STRUCTURES_TARGETED, getTotalStructuresTargetedCount(plan, childLocation));
    columns.put(STRUCTURES_SPRAYED, getTotalStructuresSprayed(reportEntry));
    columns.put(SPRAY_COVERAGE_OF_TARGETED, getSprayCoverageOfTargeted(plan, childLocation));
    columns.put(TOTAL_STRUCTURES_FOUND, getTotalStructuresFoundCount(reportEntry));
    columns.put(FOUND_COVERAGE, getFoundCoverage(plan, childLocation, reportEntry));
    columns.put(SPRAY_COVERAGE_OF_FOUND_STRUCTURES,
        getSprayCoverageFoundStructures(reportEntry));
    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }


  public List<RowData> getIRSFullDataOperational(Plan plan, Location childLocation) {
    Report reportEntry = planReportRepository.findByPlanAndLocation(plan, childLocation)
        .orElse(null);
    Map<String, ColumnData> columns = new LinkedHashMap<>();
    columns.put(TOTAL_STRUCTURES, getTotalStructuresCounts(plan, childLocation, reportEntry));
    columns.put(TOTAL_STRUCTURES_FOUND, getTotalStructuresFoundCount(reportEntry));
    columns.put(STRUCTURES_SPRAYED, getTotalStructuresSprayed(reportEntry));
    columns.put(SPRAY_COVERAGE_OF_TARGETED, getSprayCoverageOfTargeted(plan, childLocation));
    columns.put(FOUND_COVERAGE, getFoundCoverage(plan, childLocation, reportEntry));
    columns.put(SPRAY_COVERAGE_OF_FOUND_STRUCTURES,
        getSprayCoverageFoundStructures(reportEntry));
    columns.put(STRUCTURES_REMAINING_TO_SPRAY_TO_REACH_90,
        getStructuresRemainingToReach90(plan, childLocation, reportEntry));
    columns.put(REVIEWED_WITH_DECISION, getReviewedWithDecision(reportEntry));
    columns.put(MOBILIZED, getMobilized(reportEntry));
    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  public List<RowData> getIRSFullCoverageStructureLevelData(Plan plan, Location childLocation) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();
    Report report = planReportRepository.findByPlanAndLocation(plan, childLocation).orElse(null);
    columns.put(HEAD_OF_HOUSEHOLD, getHeadOfHousehold(report));
    columns.put(STRUCTURE_STATUS,
        getLocationBusinessState(report));
    columns.put(NO_OF_MALES, getMales(report));
    columns.put(NO_OF_FEMALES, getFemales(report));
    columns.put(NO_OF_PREGNANT_WOMEN, getPregnantWomen(report));
    columns.put(NO_OF_ROOMS, getRoomsSprayed(report));
    columns.put(PHONE_NUMBER, getHeadPhoneNumber(report));
    columns.put(NOT_SPRAYED_REASON, getNotSprayedReason(report));
    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  private ColumnData getHeadOfHousehold(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getHouseholdHead() != null) {
      columnData.setValue(report.getReportIndicators().getHouseholdHead());
    }
    return columnData;
  }


  public void initDataStoresIfNecessary() throws InterruptedException {
    if (!isDatastoresInitialized) {

      isDatastoresInitialized = true;
    }
  }


  private ColumnData getMobilized(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getMobilized() != null) {
      columnData.setValue(report.getReportIndicators().getMobilized());
    } else {
      columnData.setValue("No");
    }
    return columnData;
  }


  private ColumnData getReviewedWithDecision(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null) {
      columnData.setValue(report.getReportIndicators().isIrsDecisionFormFilled() ? "yes" : "no");
    }
    return columnData;
  }

  private ColumnData getStructuresRemainingToReach90(Plan plan, Location childLocation,
      Report report) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(false);
    double sprayedStructures = (double) getTotalStructuresSprayed(report).getValue();
    double totalStructures = (double) getTotalStructuresCounts(plan, childLocation,
        report).getValue();
    double structuresRemaining = Math.round((totalStructures * 0.9) - sprayedStructures);
    if (structuresRemaining < 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue(structuresRemaining);
    }
    return columnData;
  }

  private ColumnData getSprayCoverageFoundStructures(Report report) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double sprayedStructures = (double) getTotalStructuresSprayed(report).getValue();
    double foundStructures = (double) getTotalStructuresFoundCount(report).getValue();
    if (foundStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((sprayedStructures / foundStructures) * 100);
    }
    columnData.setMeta( "Sprayed Structures: "+sprayedStructures+ " / "+"Found Structures: "+foundStructures );

    return columnData;
  }

  private ColumnData getFoundCoverage(Plan plan, Location childLocation, Report report) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    double foundStructures = (double) getTotalStructuresFoundCount(report).getValue();
    double targetedStructures = (double) getTotalStructuresTargetedCount(plan,
        childLocation).getValue();
    if (targetedStructures == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue((foundStructures / targetedStructures) * 100);
    }
    columnData.setMeta("Found Structures: "+foundStructures+ " / " + "Targeted Structure: "+targetedStructures);
    return columnData;
  }

  private ColumnData getHeadPhoneNumber(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getPhoneNumber() != null) {
      ReportIndicators reportIndicators = report.getReportIndicators();
      columnData.setValue(reportIndicators.getPhoneNumber());
    }
    return columnData;
  }

  private ColumnData getNotSprayedReason(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getNotSprayedReason() != null) {
      ReportIndicators reportIndicators = report.getReportIndicators();
      columnData.setValue(reportIndicators.getNotSprayedReason());
    } else {
      columnData.setValue(N_A);
    }
    return columnData;
  }

  private ColumnData getPregnantWomen(Report report) {
    ColumnData columnData = new ColumnData();
    if (report != null) {
      ReportIndicators reportIndicators = report.getReportIndicators();
      columnData.setValue(reportIndicators.getPregnantWomen());
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  private ColumnData getRoomsSprayed(Report report) {
    ColumnData columnData = new ColumnData();
    if (report != null) {
      ReportIndicators reportIndicators = report.getReportIndicators();
      columnData.setValue(reportIndicators.getSprayedRooms());
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  private ColumnData getFemales(Report report) {
    ColumnData columnData = new ColumnData();
    if (report != null) {
      ReportIndicators reportIndicators = report.getReportIndicators();
      columnData.setValue(reportIndicators.getFemales());
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  private ColumnData getMales(Report report) {
    ColumnData columnData = new ColumnData();
    if (report != null) {
      ReportIndicators reportIndicators = report.getReportIndicators();
      columnData.setValue(reportIndicators.getMales());
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  private ColumnData getTotalStructuresSprayed(Report report) {
    ColumnData columnData = new ColumnData();
    columnData.setValue(0d);
    if (report != null && report.getReportIndicators().getSprayedStructures() != null) {
      columnData.setValue(Double.valueOf(report.getReportIndicators().getSprayedStructures()));
    }
    return columnData;
  }

  private ColumnData getSprayedEffectively(Plan plan, Location childLocation) {

    double operationalAreaVisitedCount = locationBusinessStatusService.getCountsOfVisitedLocationAboveStructure(
        plan.getLocationHierarchy().getIdentifier(), childLocation.getIdentifier(),
        plan.getIdentifier(), plan.getLocationHierarchy().getNodeOrder().get(
            plan.getLocationHierarchy().getNodeOrder().indexOf(LocationConstants.STRUCTURE) - 1));

    double operationalAreaVisitedEffectivelyCount = locationBusinessStatusService.getCountsOfVisitedEffectivelyLocationAboveStructure(
        plan.getLocationHierarchy().getIdentifier(), childLocation.getIdentifier(),
        plan.getIdentifier(), plan.getLocationHierarchy().getNodeOrder().get(
            plan.getLocationHierarchy().getNodeOrder().indexOf(LocationConstants.STRUCTURE) - 1));

    double percentageSprayedEffectively = 0;
    if (operationalAreaVisitedCount > 0) {
      percentageSprayedEffectively =
          operationalAreaVisitedEffectivelyCount / operationalAreaVisitedCount * 100;
    }

    ColumnData percentageSprayedEffectivelyColumnData = new ColumnData();
    percentageSprayedEffectivelyColumnData.setValue(percentageSprayedEffectively);
    percentageSprayedEffectivelyColumnData.setIsPercentage(true);
    percentageSprayedEffectivelyColumnData.setMeta(
        "Total Effectively Sprayed Areas : " + operationalAreaVisitedEffectivelyCount + " / "
            + "Total Spray Areas Visited: " + operationalAreaVisitedCount);

    return percentageSprayedEffectivelyColumnData;

  }

  private ColumnData getOperationalAreaVisitedCounts(Plan plan, Location childLocation) {

    double operationalAreaVisitedCount = locationBusinessStatusService.getCountsOfVisitedLocationAboveStructure(
        plan.getLocationHierarchy().getIdentifier(), childLocation.getIdentifier(),
        plan.getIdentifier(), plan.getLocationHierarchy().getNodeOrder().get(
            plan.getLocationHierarchy().getNodeOrder().indexOf(LocationConstants.STRUCTURE) - 1));
    ColumnData operationalAreaVisitedColumnData = new ColumnData();
    operationalAreaVisitedColumnData.setValue(operationalAreaVisitedCount);
    operationalAreaVisitedColumnData.setIsPercentage(false);
    return operationalAreaVisitedColumnData;
  }

  private ColumnData getTargetedAreas(Plan plan, Location childLocation) {

    Long countOfOperationalAreas = planLocationsService.getNumberOfAssignedChildrenByGeoLevelNameWithinLocationAndHierarchyAndPlan(
        plan.getIdentifier(), LocationConstants.OPERATIONAL, childLocation.getIdentifier(),
        plan.getLocationHierarchy().getIdentifier());

    Long countOfOperationalAreasValue = 0L;

    if (countOfOperationalAreas != null) {
      countOfOperationalAreasValue = countOfOperationalAreas;
    }

    ColumnData operationalAreaVisitedColumnData = new ColumnData();
    operationalAreaVisitedColumnData.setValue(countOfOperationalAreasValue);
    operationalAreaVisitedColumnData.setIsPercentage(false);
    return operationalAreaVisitedColumnData;
  }

  private ColumnData getTotalAreas(Plan plan, Location childLocation,
      String geoNameDirectlyAboveStructure) {

    Long totalOperationAreaCounts = locationBusinessStatusService.getLocationCountsForGeoLevelByHierarchyLocationParent(
        childLocation.getIdentifier(), plan.getLocationHierarchy().getIdentifier(),
        geoNameDirectlyAboveStructure, plan);

    Long totalOperationAreaCountsValue = 0L;

    if (totalOperationAreaCounts != null) {
      totalOperationAreaCountsValue = totalOperationAreaCounts;
    }

    ColumnData operationalAreaVisitedColumnData = new ColumnData();
    operationalAreaVisitedColumnData.setValue(totalOperationAreaCountsValue);
    operationalAreaVisitedColumnData.setIsPercentage(false);
    return operationalAreaVisitedColumnData;
  }

  private ColumnData getTotalStructuresCounts(Plan plan, Location childLocation, Report report) {

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

    double discoveredStructuresCount =
        (report != null && report.getReportIndicators().getRegisteredStructures() != null)
            ? Double.valueOf(report.getReportIndicators().getRegisteredStructures()) : 0d;

    double totalStructuresExcludingNotEligible =
        totalStructuresCount + discoveredStructuresCount - notEligibleStructuresCount;

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

  private ColumnData getTotalStructuresFoundCount(Report report) {
    ColumnData columnData = new ColumnData();
    if (report != null && report.getReportIndicators().getFoundStructures() != null) {
      columnData.setValue(Double.valueOf(report.getReportIndicators().getFoundStructures()));
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  private ColumnData getSprayCoverageOfTargeted(Plan plan, Location childLocation) {

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

    Long completedStructuresCountObj = null;
    LocationBusinessStateCount completedStructuresCountObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(), LocationConstants.STRUCTURE,
        BusinessStatus.COMPLETE, plan.getLocationHierarchy().getIdentifier());

    if (completedStructuresCountObjCount != null) {
      completedStructuresCountObj = completedStructuresCountObjCount.getLocationCount();
    }

    double completedStructuresCount = 0;
    if (completedStructuresCountObj != null) {
      completedStructuresCount = completedStructuresCountObj;
    }

    double percentageOfSprayedToTargeted = 0;
    if (totalStructuresInTargetedCount > 0) {
      percentageOfSprayedToTargeted =
          (completedStructuresCount / totalStructuresInTargetedCount) * 100;
    }

    ColumnData totalStructuresTargetedColumnData = new ColumnData();
    totalStructuresTargetedColumnData.setValue(percentageOfSprayedToTargeted);
    totalStructuresTargetedColumnData.setMeta(
        "Total Structures Sprayed: " + completedStructuresCount + " / "
            + "Total Structures Targeted: " + totalStructuresInTargetedCount);
    totalStructuresTargetedColumnData.setIsPercentage(true);
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
        dashboardProperties.getIrsDefaultDisplayColumns().getOrDefault(reportLevel, null));
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }


  private List<LocationResponse> setGeoJsonProperties(Map<UUID, RowData> rowDataMap,
      List<LocationResponse> locationResponses) {
    return locationResponses.stream().peek(loc -> {
      loc.getProperties().setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
      loc.getProperties().setId(loc.getIdentifier().toString());
      if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(SPRAY_COVERAGE_OF_TARGETED)
          != null) {
        loc.getProperties().setSprayCoverage(
            rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(SPRAY_COVERAGE_OF_TARGETED)
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


