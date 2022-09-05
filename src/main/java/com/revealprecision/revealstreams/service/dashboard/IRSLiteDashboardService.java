package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.props.DashboardProperties.SPRAY_COVERAGE_OF_TARGETED;
import static com.revealprecision.revealstreams.util.DashboardUtils.getBusinessStatusColor;
import static com.revealprecision.revealstreams.util.DashboardUtils.getGeoNameDirectlyAboveStructure;
import static com.revealprecision.revealstreams.util.DashboardUtils.getLocationBusinessState;
import static com.revealprecision.revealstreams.util.DashboardUtils.getStringValueColumnData;

import com.revealprecision.revealstreams.constants.EntityTagScopes;
import com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.factory.LocationResponseFactory;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.LocationCounts;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.domain.Report;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.MetadataObj;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import com.revealprecision.revealstreams.persistence.repository.ReportRepository;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.service.LocationBusinessStatusService;
import com.revealprecision.revealstreams.service.MetadataService;
import com.revealprecision.revealstreams.service.PlanLocationsService;
import com.revealprecision.revealstreams.util.DashboardUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
public class IRSLiteDashboardService {

  public static final String SPRAY_PROGRESS_SPRAYED_TARGETED = "Spray Progress(Sprayed/Targeted)";
  public static final String NUMBER_OF_SPRAY_DAYS = "Number of Spray days";
  public static final String TOTAL_SUPERVISOR_FORMS_SUBMITTED = "Total supervisor forms submitted";
  public static final String AVERAGE_STRUCTURES_PER_DAY = "Average Structures Per Day";
  public static final String AVERAGE_INSECTICIDE_USAGE_RATE = "Average Insecticide Usage Rate";
  public static final String DATE_VISITED_FOR_IRS = "Date visited for IRS";
  public static final String MOBILIZED = "Mobilized";
  public static final String DATE_MOBILIZED = "Date Mobilized";
  public static final String NO = "No";

  private final PlanLocationsService planLocationsService;
  private final LocationBusinessStatusService locationBusinessStatusService;
  private final DashboardProperties dashboardProperties;
  private final MetadataService metadataService;


  private static final String TOTAL_SPRAY_AREAS = "Total spray areas";
  private static final String TARGET_SPRAY_AREAS = "Targeted spray areas";
  private static final String VISITED_AREAS = "Total spray areas visited";
  public static final String SPRAY_COVERAGE_OF_FOUND = "Spray coverage of Found(Sprayed/Found)";
  private static final String STRUCTURES_ON_THE_GROUND = "Structures on the ground";
  private static final String TOTAL_STRUCTURES_TARGETED = "Total Structures Targeted";
  private static final String STRUCTURES_FOUND = "Structures Found";
  private static final String STRUCTURES_SPRAYED = "Structures Sprayed";
  private static final String SPRAY_AREA_VISITED = "Spray Area Visited";
  private static final String LOCATION_STATUS = "Location Status";

  private final ReportRepository planReportRepository;

  boolean datastoresInitialized = false;


  public List<RowData> getIRSFullData(Plan plan, Location childLocation) {
    Report report = planReportRepository.findByPlanAndLocation(plan, childLocation).orElse(null);
    Map<String, ColumnData> columns = new LinkedHashMap<>();
    columns.put(TOTAL_SPRAY_AREAS,
        getTotalAreas(plan, childLocation, getGeoNameDirectlyAboveStructure(plan)));
    columns.put(TARGET_SPRAY_AREAS, getTargetedAreas(plan, childLocation));
    columns.put(VISITED_AREAS, operationalAreaVisitedCounts(plan, childLocation));
    columns.put(STRUCTURES_ON_THE_GROUND, getTotalStructuresCounts(plan, childLocation));
    columns.put(TOTAL_STRUCTURES_TARGETED, getTotalStructuresTargetedCount(plan, childLocation));
    columns.put(STRUCTURES_SPRAYED, getTotalStructuresSprayed(plan,
        childLocation));
    columns.put(SPRAY_PROGRESS_SPRAYED_TARGETED,
        getSPrayedProgressTargeted(plan, childLocation));
    columns.put(STRUCTURES_FOUND, getTotalStructuresFoundCount(plan, childLocation));
    columns.put(SPRAY_COVERAGE_OF_FOUND, getSprayCoverageOfFound(plan, childLocation));
    columns.put(NUMBER_OF_SPRAY_DAYS,
        getNumberOfSprayDays(report));
    columns.put(TOTAL_SUPERVISOR_FORMS_SUBMITTED,
        getSupervisorFormSubmissions(report));
    columns.put(AVERAGE_STRUCTURES_PER_DAY,
        getAverageStructuresSprayedPerDay(plan, childLocation, report));
    columns.put(AVERAGE_INSECTICIDE_USAGE_RATE,
        getAverageInsecticideUsage(plan, childLocation, report));

    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  private ColumnData getAverageInsecticideUsage(Plan plan, Location childLocation, Report report) {
    ColumnData columnData = new ColumnData();
    Double sprayedStructures = DashboardUtils.getDouble(
        getTotalStructuresSprayed(plan, childLocation).getValue());
    Double insecticidesUsed = DashboardUtils.getDouble(getInsecticidesUsed(report).getValue());

    if (insecticidesUsed == 0) {
      columnData.setValue(0d);
    } else {
      columnData.setValue(sprayedStructures / insecticidesUsed);
    }

    columnData.setMeta("Sprayed structures: " + sprayedStructures + " / " + "Insecticides Used: "
        + insecticidesUsed);

    columnData.setIsPercentage(true);

    return columnData;
  }

  private ColumnData getInsecticidesUsed(Report report) {
    ColumnData columnData = new ColumnData();
    if (report != null && report.getReportIndicators().getInsecticidesUsed() != null) {
      columnData.setValue(report.getReportIndicators().getInsecticidesUsed());
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  private ColumnData getAverageStructuresSprayedPerDay(Plan plan, Location childLocation,
      Report report) {
    ColumnData columnData = new ColumnData();
    Double numberOfSprayDays = DashboardUtils.getDouble(getNumberOfSprayDays(report).getValue());
    Double sprayedStructures = DashboardUtils.getDouble(
        getTotalStructuresSprayed(plan, childLocation).getValue());

    if (numberOfSprayDays != null && numberOfSprayDays > 0) {
      columnData.setValue(Math.round(sprayedStructures / numberOfSprayDays));
    } else {
      columnData.setValue(0);
    }

    return columnData;
  }

  private ColumnData getSPrayedProgressTargeted(Plan plan, Location childLocation) {
    ColumnData columnData = new ColumnData();
    columnData.setIsPercentage(true);
    Double totalSprayedStructures = DashboardUtils.getDouble(getTotalStructuresSprayed(plan,
        childLocation).getValue());
    Double totalTargetedStructures = DashboardUtils.getDouble(getTotalStructuresTargetedCount(plan,
        childLocation).getValue());

    if (totalTargetedStructures != null && totalTargetedStructures != 0) {
      columnData.setValue((totalSprayedStructures / totalTargetedStructures) * 100);
    } else {
      columnData.setValue(0);
    }
    columnData.setMeta("Total Structures Sprayed : " + totalSprayedStructures + " / "
        + "Total Targeted Structures: " + totalTargetedStructures);
    return columnData;
  }

  private ColumnData getNumberOfSprayDays(Report report) {
    ColumnData columnData = new ColumnData();

    if (report != null && report.getReportIndicators().getUniqueSupervisionDates() != null) {
      columnData.setValue(
          (double) report.getReportIndicators().getUniqueSupervisionDates().size());
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  private ColumnData getSupervisorFormSubmissions(Report report) {
    ColumnData columnData = new ColumnData();
    if (report != null && report.getReportIndicators().getSupervisorFormSubmissionCount() != null) {
      columnData.setValue(report.getReportIndicators().getSupervisorFormSubmissionCount());
    } else {
      columnData.setValue(0d);
    }
    return columnData;
  }

  public List<RowData> getIRSFullDataOperational(Plan plan, Location childLocation) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();
    Report report = planReportRepository.findByPlanAndLocation(plan, childLocation).orElse(null);
    columns.put(SPRAY_AREA_VISITED, getAreaVisitedInSprayArea(report));
    columns.put(DATE_VISITED_FOR_IRS, getSprayDate(report));
    columns.put(STRUCTURES_ON_THE_GROUND, getTotalStructuresCounts(plan, childLocation));
    columns.put(MOBILIZED, getMobilized(report));
    columns.put(DATE_MOBILIZED, getMobilizedDate(report));
    columns.put(LOCATION_STATUS, getLocationBusinessState(report));
    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }


  public void initDataStoresIfNecessary() throws InterruptedException {
    if (!datastoresInitialized) {
      datastoresInitialized = true;
    }
  }

  public static void main(String[] args) {
    String date = "02-09-2022";
    LocalDate localDateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    System.out.println(localDateTime);
  }

  private ColumnData getSprayDate(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getDateSprayed() != null) {

      columnData.setValue(report.getReportIndicators().getDateSprayed());
    }
    return columnData;
  }

  private ColumnData getMobilizedDate(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getMobilizationDate() != null) {
      String mobilizationDate = report.getReportIndicators().getMobilizationDate();
      try {
        columnData.setValue(
            LocalDate.parse(mobilizationDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")));
      } catch (RuntimeException e) {
        log.warn("Mobilized Date not in the correct format - {}", mobilizationDate);
        columnData.setValue(mobilizationDate);
      }
    }
    return columnData;
  }


  private ColumnData getMobilized(Report report) {
    ColumnData columnData = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getMobilized() != null) {
      columnData.setValue(report.getReportIndicators().getMobilized());
    } else {
      columnData.setValue(NO);
    }
    return columnData;
  }

  private ColumnData getTotalStructuresSprayed(Plan plan, Location childLocation) {

    List<MetadataObj> locationMetadataByTagName = metadataService.getLocationMetadataByTagName(
        childLocation.getIdentifier(), plan.getIdentifier(), "irs-lite-sprayed-sum", null,
        EntityTagScopes.PLAN);

    double sprayedLocationsCount = 0;
    if (locationMetadataByTagName != null) {
      if (locationMetadataByTagName.get(0) != null) {
        sprayedLocationsCount = locationMetadataByTagName.get(0).getCurrent().getValue()
            .getValueDouble();
      }
    }

    ColumnData totalStructuresFoundColumnData = new ColumnData();
    totalStructuresFoundColumnData.setValue(sprayedLocationsCount);
    totalStructuresFoundColumnData.setIsPercentage(false);
    return totalStructuresFoundColumnData;

  }

  private ColumnData operationalAreaVisitedCounts(Plan plan, Location childLocation) {

    Long sprayedLocationsObj = null;
    LocationBusinessStateCount sprayedLocationsObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(),
        plan.getPlanTargetType().getGeographicLevel().getName(),
        BusinessStatus.SPRAYED, plan.getLocationHierarchy().getIdentifier());

    if (sprayedLocationsObjCount != null) {
      sprayedLocationsObj = sprayedLocationsObjCount.getLocationCount();
    }

    double sprayedLocationsCount = 0;
    if (sprayedLocationsObj != null) {
      sprayedLocationsCount = sprayedLocationsObj;
    }

    Long notSprayedLocationsObj = null;
    LocationBusinessStateCount notSprayedLocationsObjCount = locationBusinessStatusService.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(),
        plan.getPlanTargetType().getGeographicLevel().getName(),
        BusinessStatus.NOT_SPRAYED, plan.getLocationHierarchy().getIdentifier());

    if (notSprayedLocationsObjCount != null) {
      notSprayedLocationsObj = notSprayedLocationsObjCount.getLocationCount();
    }

    double notSprayedLocationsCount = 0;
    if (notSprayedLocationsObj != null) {
      notSprayedLocationsCount = notSprayedLocationsObj;
    }

    double visitedAreas = sprayedLocationsCount + notSprayedLocationsCount;

    ColumnData operationalAreaVisitedColumnData = new ColumnData();
    operationalAreaVisitedColumnData.setValue(visitedAreas);
    operationalAreaVisitedColumnData.setIsPercentage(false);
    return operationalAreaVisitedColumnData;
  }

  private ColumnData getTargetedAreas(Plan plan, Location childLocation) {

    Long countOfOperationalAreas = planLocationsService.getNumberOfAssignedChildrenByGeoLevelNameWithinLocationAndHierarchyAndPlan(
        plan.getIdentifier(), plan.getPlanTargetType().getGeographicLevel().getName(),
        childLocation.getIdentifier(),
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

    Long totalOperationAreaCounts = null;
    LocationCounts locationCounts = locationBusinessStatusService.getLocationCountsForGeoLevelByHierarchyLocationParent(
        childLocation.getIdentifier(), plan.getLocationHierarchy().getIdentifier(),
        plan.getPlanTargetType().getGeographicLevel().getName());

    if (locationCounts != null) {
      totalOperationAreaCounts = locationCounts.getLocationCount();
    }

    Long totalOperationAreaCountsValue = 0L;

    if (totalOperationAreaCounts != null) {
      totalOperationAreaCountsValue = totalOperationAreaCounts;
    }

    ColumnData operationalAreaVisitedColumnData = new ColumnData();
    operationalAreaVisitedColumnData.setValue(totalOperationAreaCountsValue);
    operationalAreaVisitedColumnData.setIsPercentage(false);
    return operationalAreaVisitedColumnData;
  }

  private ColumnData getTotalStructuresCounts(Plan plan, Location childLocation) {

    Long totalStructuresCountObj = null;
    LocationCounts locationCounts = locationBusinessStatusService.getLocationCountsForGeoLevelByHierarchyLocationParent(
        childLocation.getIdentifier(), plan.getLocationHierarchy().getIdentifier(),
        LocationConstants.STRUCTURE);

    if (locationCounts != null) {
      totalStructuresCountObj = locationCounts.getLocationCount();
    }

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

    double totalStructuresExcludingNotEligible = totalStructuresCount - notEligibleStructuresCount;

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

    List<MetadataObj> locationMetadataByTagName = metadataService.getLocationMetadataByTagName(
        childLocation.getIdentifier(), plan.getIdentifier(), "irs-lite-found-sum", null,
        EntityTagScopes.PLAN);

    double sprayedLocationsCount = 0;

    double notSprayedLocationsCount = 0;

    double totalStructuresFound = 0;

    if (locationMetadataByTagName != null) {
      Double totalStructuresFoundValue = locationMetadataByTagName.get(0).getCurrent().getValue()
          .getValueDouble();
      if (totalStructuresFoundValue != null) {
        totalStructuresFound = totalStructuresFoundValue;
      }
    }

    ColumnData totalStructuresFoundColumnData = new ColumnData();
    totalStructuresFoundColumnData.setValue(totalStructuresFound);
    totalStructuresFoundColumnData.setIsPercentage(false);
    return totalStructuresFoundColumnData;
  }


  private ColumnData getAreaVisitedInSprayArea(Report report) {

    ColumnData areaVisitedColumnData = new ColumnData();
    String areaVisited = "no";

    if (report != null && report.getReportIndicators().getBusinessStatus() != null) {
      if (report.getReportIndicators().getBusinessStatus()
          .equals(BusinessStatus.SPRAYED)) {
        areaVisited = "yes";
      }
    }

    areaVisitedColumnData.setValue(areaVisited);

    areaVisitedColumnData.setValue(areaVisited);
    areaVisitedColumnData.setIsPercentage(false);
    areaVisitedColumnData.setDataType("string");
    return areaVisitedColumnData;
  }


  private ColumnData getSprayCoverageOfFound(Plan plan, Location childLocation) {

    List<MetadataObj> locationMetadataByTagNameFound = metadataService.getLocationMetadataByTagName(
        childLocation.getIdentifier(), plan.getIdentifier(), "irs-lite-found-sum", null,
        EntityTagScopes.PLAN);

    double totalStructuresFound = 0;

    if (locationMetadataByTagNameFound != null) {
      Double totalStructuresFoundValue = locationMetadataByTagNameFound.get(0).getCurrent()
          .getValue()
          .getValueDouble();
      if (totalStructuresFoundValue != null) {
        totalStructuresFound = totalStructuresFoundValue;
      }
    }

    List<MetadataObj> locationMetadataByTagNameSprayed = metadataService.getLocationMetadataByTagName(
        childLocation.getIdentifier(), plan.getIdentifier(), "irs-lite-sprayed-sum", null,
        EntityTagScopes.PLAN);

    double sprayedLocationsCount = 0;
    if (locationMetadataByTagNameSprayed != null) {
      if (locationMetadataByTagNameSprayed.get(0) != null) {
        sprayedLocationsCount = locationMetadataByTagNameSprayed.get(0).getCurrent().getValue()
            .getValueDouble();
      }
    }

    double sprayCoverageOfFound = 0;

    if (totalStructuresFound > 0) {
      sprayCoverageOfFound = sprayedLocationsCount / totalStructuresFound * 100;
    }

    ColumnData sprayCoverageOfFoundColumnData = new ColumnData();
    sprayCoverageOfFoundColumnData.setValue(sprayCoverageOfFound);
    sprayCoverageOfFoundColumnData.setMeta(
        "Total Structures Sprayed: " + sprayedLocationsCount + " / " + "Total Structures Found: "
            + totalStructuresFound);
    sprayCoverageOfFoundColumnData.setIsPercentage(true);
    return sprayCoverageOfFoundColumnData;
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
          .get(LOCATION_STATUS) != null) {
        String businessStatus = (String) rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
            .get(LOCATION_STATUS).getValue();
        loc.getProperties().setBusinessStatus(
            businessStatus);
        loc.getProperties().setStatusColor(getBusinessStatusColor(businessStatus));
      }
    }).collect(Collectors.toList());
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
        dashboardProperties.getIrsLiteDefaultDisplayColumns().getOrDefault(reportLevel, null));
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }
}
