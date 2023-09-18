package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.constants.DashboardColumns.ABSENT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.ADMINISTERED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.BUSINESS_STATUS;
import static com.revealprecision.revealstreams.constants.DashboardColumns.COVERAGE_OF_STRUCTURES_COMPLETED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.COVERAGE_OF_STRUCTURES_VISITED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FEMALES_15;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FEMALES_5_14;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FIELD_VERIFIED_POP_TARGET;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FIELD_VERIFIED_POP_TREATMENT_COVERAGE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.HEAD_OF_HOUSE_HOLD;
import static com.revealprecision.revealstreams.constants.DashboardColumns.HOUSEHOLD_DISTRIBUTION;
import static com.revealprecision.revealstreams.constants.DashboardColumns.LOST_DAMAGED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.MALES_15;
import static com.revealprecision.revealstreams.constants.DashboardColumns.MALES_5_14;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NUMBER_OF_ADVERSE_EVENTS;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NUMBER_OF_STRUCTURES_WITHIN_HOUSEHOLD;
import static com.revealprecision.revealstreams.constants.DashboardColumns.OFFICIAL_POP_TARGET;
import static com.revealprecision.revealstreams.constants.DashboardColumns.OFFICIAL_POP_TREATMENT_COVERAGE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.PHONE_NUMBER;
import static com.revealprecision.revealstreams.constants.DashboardColumns.POINT_DISTRIBUTION;
import static com.revealprecision.revealstreams.constants.DashboardColumns.PREGNANT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.RECEIVED_BY_CDD;
import static com.revealprecision.revealstreams.constants.DashboardColumns.REFUSAL;
import static com.revealprecision.revealstreams.constants.DashboardColumns.RETURNED_TO_SUPERVISOR;
import static com.revealprecision.revealstreams.constants.DashboardColumns.SICK;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_COMPLETE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_NOT_YET_VISITED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_PARTIALLY_COMPLETE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_REFUSED_ABSENT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_VISITED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_LIVING_ON_THE_STREET;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_STRUCTURE_COUNT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_TREATED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_UNTREATED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TRAVEL;
import static com.revealprecision.revealstreams.util.DashboardUtils.getBusinessStatusColor;

import com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus;
import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.factory.LocationResponseFactory;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import com.revealprecision.revealstreams.persistence.projection.LocationMetadataDoubleAggregateProjection;
import com.revealprecision.revealstreams.persistence.projection.OnchocerciasisSurveyAdverseEventsAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.OnchocerciasisSurveyCddSummaryAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.OnchocerciasisSurveyDrugAccountabilityAggregationProjection;
import com.revealprecision.revealstreams.persistence.repository.EventTrackerRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationMetadataRepository;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.service.LocationBusinessStatusService;
import com.revealprecision.revealstreams.service.PlanLocationsService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class OnchocerciasisDashboardService {

  public static final String ONCHOCERCIASIS = "Onchocerciasis";
  public static final String SCH = "SCH";


  public static final String NTD = "ntd";
  public static final String LOCATION = "LOCATION";

  private final DashboardProperties dashboardProperties;
  private final EventTrackerRepository eventTrackerRepository;
  private final LocationBusinessStatusService locationBusinessStatusService;
  private final PlanLocationsService planLocationsService;

  private final LocationMetadataRepository locationMetadataRepository;


  public List<RowData> getMDALiteCoverageData(Plan plan, Location childLocation,
      MdaLiteReportType type) {

    OnchocerciasisSurveyCddSummaryAggregationProjection onchocerciasisSurvey = eventTrackerRepository.getOnchoSurveyFromCddSummary(
        childLocation.getIdentifier(), plan.getIdentifier());

    List<OnchocerciasisSurveyCddSummaryAggregationProjection> aggregationDataFromTreatmentOutsideHousehold = eventTrackerRepository.getOnchoSurveyFromTreatmentOutsideHousehold(
        childLocation.getIdentifier(), plan.getIdentifier());

    OnchocerciasisSurveyDrugAccountabilityAggregationProjection onchoSurveyFromDrugAccountability = eventTrackerRepository.getOnchoSurveyFromDrugAccountability(
        childLocation.getIdentifier(), plan.getIdentifier());

    OnchocerciasisSurveyAdverseEventsAggregationProjection onchoSurveyFromAdverseEventsRecord = eventTrackerRepository.getOnchoSurveyFromAdverseEventsRecord(
        childLocation.getIdentifier(), plan.getIdentifier());

    Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap = locationBusinessStatusService.getLocationBusinessStateObjPerGeoLevel(
        plan.getIdentifier(), childLocation.getIdentifier(),
        childLocation.getGeographicLevel().getName(), plan.getLocationHierarchy().getIdentifier());

    LocationMetadataDoubleAggregateProjection locationMetadataDoubleAggregateProjectionOnTargetLevel = getLocationMetadataDoubleAggregateProjection(
        childLocation.getIdentifier().toString());

    OnchocerciasisSurveyCddSummaryAggregationProjection pointDistributionData = getHomelessOrPointDistributionColumnData(
        aggregationDataFromTreatmentOutsideHousehold, "Point Distribution");

    OnchocerciasisSurveyCddSummaryAggregationProjection homelessIndividuals = getHomelessOrPointDistributionColumnData(
        aggregationDataFromTreatmentOutsideHousehold,
        "Homeless individuals");

    Map<String, ColumnData> columns = new HashMap<>();
    if (type == MdaLiteReportType.DRUG_DISTRIBUTION) {
      columns = getDrugDistributionDashboardData(
          onchoSurveyFromDrugAccountability,
          onchoSurveyFromAdverseEventsRecord,
          locationBusinessStateObjPerGeoLevelMap,
          onchocerciasisSurvey,
          pointDistributionData,
          homelessIndividuals);
    } else if (type == MdaLiteReportType.TREATMENT_COVERAGE) {
      columns = getTreatmentCoverageDashboardData(onchocerciasisSurvey,
          locationMetadataDoubleAggregateProjectionOnTargetLevel,
          plan,
          childLocation,
          locationBusinessStateObjPerGeoLevelMap,
          pointDistributionData,
          homelessIndividuals
      );
    } else if (type == MdaLiteReportType.POPULATION_DISTRIBUTION) {
      columns = getPopulationDistributionDashboardData(onchocerciasisSurvey,
          pointDistributionData,
          homelessIndividuals,
          locationBusinessStateObjPerGeoLevelMap
      );
    }

    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }


  private LocationMetadataDoubleAggregateProjection getLocationMetadataDoubleAggregateProjectionOnTargetLevel(
      String locationIdentifier) {
    int sum = 0;

    return locationMetadataRepository.getValueOfDoubleTagByLocationIdentifierAndTagOnTargetLevel(
        locationIdentifier,
        dashboardProperties.getOnchoImportTag());


  }

  private LocationMetadataDoubleAggregateProjection getLocationMetadataDoubleAggregateProjection(
      String locationIdentifier) {
    int sum = 0;

    return locationMetadataRepository.getSumOfDoubleTagByLocationIdentifierAndTag(
        locationIdentifier,
        dashboardProperties.getOnchoImportTag());


  }


  private double getTotalStructureCountValue(Plan plan, Location childLocation,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap) {
    Long totalStructuresTargetedCountObj = planLocationsService.getAssignedStructureCountByLocationParentAndPlan(
        plan, childLocation);

    double totalStructuresInPlanLocationCount = 0;
    if (totalStructuresTargetedCountObj != null) {
      totalStructuresInPlanLocationCount = totalStructuresTargetedCountObj;
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

    return totalStructuresInPlanLocationCount - notEligibleStructuresCount;
  }


  private Map<String, ColumnData> getPopulationDistributionDashboardData(
      OnchocerciasisSurveyCddSummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      OnchocerciasisSurveyCddSummaryAggregationProjection pointDistributionData,
      OnchocerciasisSurveyCddSummaryAggregationProjection homelessIndividuals,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap
  ) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    Double totalTreated = getTotalTreatedAccrossHoldAndOutside(
        aggregationDataFromCddSupervisorDailySummary,
        pointDistributionData, homelessIndividuals);

    Long visited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> !Objects.equals(key,
            BusinessStatus.NOT_VISITED)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long completed = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.MDA_COMPLETE)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long notVisited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.NOT_VISITED)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long total = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));



    columns.put(HOUSEHOLD_DISTRIBUTION,
        new ColumnData().setValue(
            aggregationDataFromCddSupervisorDailySummary == null ? 0
                : aggregationDataFromCddSupervisorDailySummary.getTotalTreated()));

    columns.put(POINT_DISTRIBUTION,
        new ColumnData().setValue(
            pointDistributionData == null ? 0 : pointDistributionData.getTotalTreated()));

    columns.put(TOTAL_LIVING_ON_THE_STREET,
        new ColumnData().setValue(
            homelessIndividuals == null ? 0 : homelessIndividuals.getTotalTreated()));

    columns.put(MALES_5_14,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0
                : homelessIndividuals.getTotalTreatedMaleFiveFourteen()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalTreatedMaleFiveFourteen()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalTreatedMaleFiveFourteen())));

    columns.put(FEMALES_5_14,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0
                : homelessIndividuals.getTotalTreatedFemaleFiveFourteen()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalTreatedFemaleFiveFourteen()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalTreatedFemaleFiveFourteen())));

    columns.put(MALES_15,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0
                : homelessIndividuals.getTotalTreatedMaleAboveFifteen()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalTreatedMaleAboveFifteen()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalTreatedMaleAboveFifteen())));

    columns.put(FEMALES_15,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0
                : homelessIndividuals.getTotalTreatedFemaleAboveFifteen()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalTreatedFemaleAboveFifteen()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalTreatedFemaleAboveFifteen())));

    columns.put(TOTAL_UNTREATED,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0 : homelessIndividuals.getTotalUntreated()) +
                (pointDistributionData == null ? 0 : pointDistributionData.getTotalUntreated()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalUntreated())));

    columns.put(PREGNANT,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0 : homelessIndividuals.getTotalUntreatedPregnant()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalUntreatedPregnant()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalUntreatedPregnant())));

    columns.put(SICK,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0 : homelessIndividuals.getTotalUntreatedSick()) +
                (pointDistributionData == null ? 0 : pointDistributionData.getTotalUntreatedSick())
                +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalUntreatedSick())));

    columns.put(ABSENT,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0 : homelessIndividuals.getTotalUntreatedAbsent()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalUntreatedAbsent()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalUntreatedAbsent())));

    columns.put(REFUSAL,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0 : homelessIndividuals.getTotalUntreatedRefusal()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalUntreatedRefusal()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalUntreatedRefusal())));

    columns.put(TRAVEL,
        new ColumnData().setValue(
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalUntreatedTravel())));

    columns.put(COVERAGE_OF_STRUCTURES_VISITED,
        getCoverageOfStructuresVisited(visited, total).setIsHidden(true));

    columns.put(COVERAGE_OF_STRUCTURES_COMPLETED,
        getCoverageOfStructuresCompleted(completed, total).setIsHidden(true));

    columns.put(FIELD_VERIFIED_POP_TREATMENT_COVERAGE,
        getTreatmentCoverage(
            totalTreated, getFieldVerifiedPopTarget(locationBusinessStateObjPerGeoLevelMap,
                totalTreated)).setIsHidden(true));

    return columns;
  }


  private Map<String, ColumnData> getOperationalData(
      OnchocerciasisSurveyCddSummaryAggregationProjection onchoSurveyFromHouseholdHeadData) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(LOCATION,
        new ColumnData().setIsHidden(true).setDataType("string").setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getLocationIdentifier())));

    columns.put(HEAD_OF_HOUSE_HOLD,
        new ColumnData().setDataType("string").setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getHouseholdHead())));

    columns.put(PHONE_NUMBER,
        new ColumnData().setDataType("string").setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getHouseholdHeadPhoneNumber())));

    columns.put(NUMBER_OF_STRUCTURES_WITHIN_HOUSEHOLD,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getNumberOfStructures())));

    columns.put(TOTAL_TREATED,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalTreated())));

    columns.put(MALES_5_14,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalTreatedMaleFiveFourteen())));

    columns.put(FEMALES_5_14,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalTreatedFemaleFiveFourteen())));

    columns.put(MALES_15,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalTreatedMaleAboveFifteen())));

    columns.put(FEMALES_15,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalTreatedFemaleAboveFifteen())));

    columns.put(TOTAL_UNTREATED,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalTreated())));

    columns.put(PREGNANT,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalUntreatedPregnant())));

    columns.put(SICK,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalUntreatedSick())));

    columns.put(ABSENT,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalUntreatedAbsent())));

    columns.put(REFUSAL,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTotalUntreatedRefusal())));

    columns.put(ADMINISTERED,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getTablets())));

    columns.put(BUSINESS_STATUS,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0
                : onchoSurveyFromHouseholdHeadData.getBusinessStatus())).setIsHidden(true));

    return columns;
  }


  private Map<String, ColumnData> getDrugDistributionDashboardData(
      OnchocerciasisSurveyDrugAccountabilityAggregationProjection onchoSurveyFromDrugAccountability,
      OnchocerciasisSurveyAdverseEventsAggregationProjection onchoSurveyFromAdverseEventsRecord,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap,
      OnchocerciasisSurveyCddSummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      OnchocerciasisSurveyCddSummaryAggregationProjection pointDistributionData,
      OnchocerciasisSurveyCddSummaryAggregationProjection homelessIndividuals
  ) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    Double totalTreated = getTotalTreatedAccrossHoldAndOutside(
        aggregationDataFromCddSupervisorDailySummary,
        pointDistributionData, homelessIndividuals);

    Long visited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> !Objects.equals(key,
            BusinessStatus.NOT_VISITED)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long completed = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.MDA_COMPLETE)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long notVisited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.NOT_VISITED)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long total = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    columns.put(getColumnName(RECEIVED_BY_CDD),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability == null ? 0
            : onchoSurveyFromDrugAccountability.getTabletsReceived()));

    columns.put(getColumnName(ADMINISTERED),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability == null ? 0
            : onchoSurveyFromDrugAccountability.getTabletsUsed()));

    columns.put(getColumnName(LOST_DAMAGED),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability == null ? 0
            : onchoSurveyFromDrugAccountability.getTabletsLost()));

    columns.put(getColumnName(RETURNED_TO_SUPERVISOR),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability == null ? 0
            : onchoSurveyFromDrugAccountability.getTabletsReturned()));

    columns.put(getColumnName(NUMBER_OF_ADVERSE_EVENTS),
        new ColumnData().setValue(onchoSurveyFromAdverseEventsRecord == null ? 0
            : onchoSurveyFromAdverseEventsRecord.getAdverseEventCount()));

    columns.put(COVERAGE_OF_STRUCTURES_VISITED,
        getCoverageOfStructuresVisited(visited, total).setIsHidden(true));

    columns.put(COVERAGE_OF_STRUCTURES_COMPLETED,
        getCoverageOfStructuresCompleted(completed, total).setIsHidden(true));

    columns.put(FIELD_VERIFIED_POP_TREATMENT_COVERAGE,
        getTreatmentCoverage(
            totalTreated, getFieldVerifiedPopTarget(locationBusinessStateObjPerGeoLevelMap,
                totalTreated)).setIsHidden(true));

    return columns;
  }

  private String getColumnName(String column) {
    return column;
  }

  private Map<String, ColumnData> getTreatmentCoverageDashboardData(
      OnchocerciasisSurveyCddSummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      LocationMetadataDoubleAggregateProjection locationMetadataDoubleAggregateProjection,
      Plan plan,
      Location location,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap,
      OnchocerciasisSurveyCddSummaryAggregationProjection pointDistributionData,
      OnchocerciasisSurveyCddSummaryAggregationProjection homelessIndividuals
  ) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    Double totalTreated = getTotalTreatedAccrossHoldAndOutside(
        aggregationDataFromCddSupervisorDailySummary,
        pointDistributionData, homelessIndividuals);

    Double totalStructureCountValue = getTotalStructureCountValue(plan, location,
        locationBusinessStateObjPerGeoLevelMap);

    ColumnData censusPopTarget = getCensusPopTarget(locationMetadataDoubleAggregateProjection);

    Long visited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> !Objects.equals(key,
            BusinessStatus.NOT_VISITED) && !Objects.equals(key,
            BusinessStatus.NOT_ELIGIBLE)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long completed = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.MDA_COMPLETE)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long notVisited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.NOT_VISITED)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long total = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> !Objects.equals(key,
            BusinessStatus.NOT_ELIGIBLE))
        .map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    columns.put(OFFICIAL_POP_TARGET,
        censusPopTarget);

    columns.put(FIELD_VERIFIED_POP_TARGET,
        getFieldVerifiedPopTarget(locationBusinessStateObjPerGeoLevelMap, totalTreated));

    columns.put(TOTAL_TREATED,
        new ColumnData()
            .setValue(totalTreated).setMeta(getTotalTreatedAccrossHoldAndOutsideMeta(
                aggregationDataFromCddSupervisorDailySummary,
                pointDistributionData, homelessIndividuals)));

    columns.put(OFFICIAL_POP_TREATMENT_COVERAGE,
        getTreatmentCoverage(
            totalTreated, censusPopTarget));

    columns.put(FIELD_VERIFIED_POP_TREATMENT_COVERAGE,
        getTreatmentCoverage(
            totalTreated,
            getFieldVerifiedPopTarget(locationBusinessStateObjPerGeoLevelMap, totalTreated)));

    columns.put(TOTAL_STRUCTURE_COUNT,
        new ColumnData()
            .setValue(totalStructureCountValue));

    columns.put(STRUCTURES_COMPLETE,
        new ColumnData().setValue(getBusinessStatusCount(locationBusinessStateObjPerGeoLevelMap,
            BusinessStatus.MDA_COMPLETE)));

    columns.put(STRUCTURES_VISITED,
        new ColumnData().setValue(visited));

    columns.put(STRUCTURES_PARTIALLY_COMPLETE,
        new ColumnData().setValue(getBusinessStatusCount(locationBusinessStateObjPerGeoLevelMap,
            BusinessStatus.MDA_PARTIALLY_COMPLETE)));

    columns.put(STRUCTURES_REFUSED_ABSENT,
        new ColumnData().setValue(getBusinessStatusCount(locationBusinessStateObjPerGeoLevelMap,
            BusinessStatus.MDA_REFUSED_OR_ABSENT)));

    columns.put(STRUCTURES_NOT_YET_VISITED,
        new ColumnData().setValue(getBusinessStatusCount(locationBusinessStateObjPerGeoLevelMap,
            BusinessStatus.NOT_VISITED)));

    columns.put(COVERAGE_OF_STRUCTURES_VISITED,
        getCoverageOfStructuresVisited(visited, total));

    columns.put(COVERAGE_OF_STRUCTURES_COMPLETED,
        getCoverageOfStructuresCompleted(completed, total));

    return columns;
  }

  private ColumnData getCoverageOfStructuresCompleted(Long completed, Long total) {

    double value = 0;
    if (total > 0) {
      value = (double) completed / (double) total * 100;
    }

    return new ColumnData().setValue(value)
        .setMeta("Completed: " + completed + " / " + " Total: " + total).setIsPercentage(true);
  }

  private ColumnData getCoverageOfStructuresVisited(Long visited, Long total) {
    double value = 0;
    if (total > 0) {
      value = (double) visited / (double) total * 100;
    }

    return new ColumnData().setValue(value)
        .setMeta("Visited: " + visited + " / " + " Total: " + total).setIsPercentage(true);
  }

  private ColumnData getFieldVerifiedPopTarget(
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap,
      Double totalTreated) {
    Long visited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> !Objects.equals(key,
            BusinessStatus.NOT_VISITED)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long completed = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.MDA_COMPLETE)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long notVisited = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .filter(key -> Objects.equals(key,
            BusinessStatus.NOT_VISITED)).map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    Long total = locationBusinessStateObjPerGeoLevelMap.keySet().stream()
        .map(locationBusinessStateObjPerGeoLevelMap::get)
        .collect(Collectors.summingLong(o -> o.getLocationCount()));

    return getFieldVerifiedPopTarget(totalTreated,
        locationBusinessStateObjPerGeoLevelMap, visited, notVisited, total);
  }


  private Double getTotalTreatedAccrossHoldAndOutside(
      OnchocerciasisSurveyCddSummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      OnchocerciasisSurveyCddSummaryAggregationProjection pointDistributionData,
      OnchocerciasisSurveyCddSummaryAggregationProjection homelessIndividuals) {
    int pointTreated = pointDistributionData == null ? 0 : pointDistributionData.getTotalTreated();

    int homelessTreated = homelessIndividuals == null ? 0 : homelessIndividuals.getTotalTreated();

    int householdTreated = aggregationDataFromCddSupervisorDailySummary == null ? 0
        : aggregationDataFromCddSupervisorDailySummary.getTotalTreated();

    return (double) pointTreated + (double) homelessTreated + (double) householdTreated;
  }

  private String getTotalTreatedAccrossHoldAndOutsideMeta(
      OnchocerciasisSurveyCddSummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      OnchocerciasisSurveyCddSummaryAggregationProjection pointDistributionData,
      OnchocerciasisSurveyCddSummaryAggregationProjection homelessIndividuals) {
    int pointTreated = pointDistributionData == null ? 0 : pointDistributionData.getTotalTreated();

    int homelessTreated = homelessIndividuals == null ? 0 : homelessIndividuals.getTotalTreated();

    int householdTreated = aggregationDataFromCddSupervisorDailySummary == null ? 0
        : aggregationDataFromCddSupervisorDailySummary.getTotalTreated();

    return "Point Treated: " + pointTreated + " Homeless Treated: " + homelessTreated
        + " Household Treated: " + householdTreated;
  }

  public List<RowData> getMDALiteCoverageDataAboveStructureLevel(Plan plan, Location childLocation,
      MdaLiteReportType type, Location parentLocation) {

    List<OnchocerciasisSurveyCddSummaryAggregationProjection> onchoSurveyFromHouseholdHeadDataList = eventTrackerRepository.getOnchoSurveyFromHouseholdHeadData(
        parentLocation.getIdentifier(), plan.getIdentifier());

    List<RowData> collect = onchoSurveyFromHouseholdHeadDataList.stream()
        .filter(onchocerciasisSurveyCddSummaryAggregationProjection -> onchocerciasisSurveyCddSummaryAggregationProjection.getHouseholdHead()!=null)
        .map(this::getOperationalData)
        .map(stringColumnDataMap -> {
          RowData rowData = new RowData();
          rowData.setLocationIdentifier(
              UUID.fromString((String) stringColumnDataMap.get(LOCATION).getValue()));
          rowData.setColumnDataMap(stringColumnDataMap);
          rowData.setLocationName((String) stringColumnDataMap.get(HEAD_OF_HOUSE_HOLD).getValue());
          return rowData;
        })
        .collect(Collectors.toList());
    return collect;
  }


  private ColumnData getTreated(
      Double treated) {
    return new ColumnData().setValue(treated);
  }


  private ColumnData getDummy() {
    return new ColumnData().setValue(0);
  }


  private OnchocerciasisSurveyCddSummaryAggregationProjection getHomelessOrPointDistributionColumnData(
      List<OnchocerciasisSurveyCddSummaryAggregationProjection> aggregationDataFromTreatmentOutsideHouseholdList,
      String treatmentLocationType) {
    Optional<OnchocerciasisSurveyCddSummaryAggregationProjection> optionalData = aggregationDataFromTreatmentOutsideHouseholdList.stream()
        .filter(
            aggregationDataFromTreatmentOutsideHousehold -> aggregationDataFromTreatmentOutsideHousehold.getTreatmentLocationType()
                .equals(treatmentLocationType)).findFirst();
    return optionalData.orElse(null);

  }

  private ColumnData getStructureBusinessStatusColumnData(
      Long businessStatusCount) {
    return new ColumnData().setValue(businessStatusCount);
  }

  private Long getBusinessStatusCount(
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap,
      String businessStatus) {
    LocationBusinessStateCount locationBusinessStateCount = locationBusinessStateObjPerGeoLevelMap.get(
        businessStatus);
    return locationBusinessStateCount == null ? 0 : locationBusinessStateCount.getLocationCount();
  }


  private ColumnData getHiddenColumn(Object value) {
    return new ColumnData().setIsHidden(true).setValue(value);
  }

  private ColumnData getTreatmentCoverage(
      Double totalTreated,
      ColumnData poptarget) {

    Double value;
    ColumnData columnData = new ColumnData().setIsPercentage(true);

    if (poptarget.getValue() != null) {
      if (poptarget.getValue() instanceof Double) {
        value = (Double) poptarget.getValue();
      } else {
        value = ((Integer) poptarget.getValue()).doubleValue();
      }

      if (value > 0) {
        if (totalTreated > 0) {

          Double treatmentCoverage = totalTreated
              / value * 100;
          String meta = "Treated: " + totalTreated + " / " + "Census Target " + ": "
              + poptarget.getValue();
          return columnData.setValue(treatmentCoverage).setMeta(meta)
              .setDataType("double");
        }
      }
    }
    return columnData.setValue(0)
        .setMeta("Treated: " + totalTreated + " / " + "Census Target " + ": "
            + poptarget.getValue());
  }


  private ColumnData getCensusPopTarget(
      LocationMetadataDoubleAggregateProjection locationMetadataDoubleAggregateProjection) {

    int sum = 0;
    if (locationMetadataDoubleAggregateProjection != null) {
      sum = locationMetadataDoubleAggregateProjection.getValue();
    }

    return new ColumnData().setValue(sum);
  }

  private ColumnData getFieldVerifiedPopTarget(
      Double totalTreated,
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap, Long visited,
      Long notVisited, Long total) {

    double averageTreated = dashboardProperties.getOnchoAveragePerStructure();
    String meta = "";
    if (total > 0) {
      if ((double) visited / (double) total * 100
          > dashboardProperties.getOnchoMinFieldVerifiedPercentage()) {
        averageTreated = (double) totalTreated / (double) visited;
        meta =
            meta + "averageTreated(total treated :" + totalTreated + " / " + " visited: " + visited
                + ") ";
      } else {
        meta = meta + "averageTreated(" + averageTreated + ")";
      }
    }
    double averageRemainingToBeTreated = (double) averageTreated * (double) notVisited;
    meta = "(" + meta + ")" + " x " + "not visited: " + notVisited;
    double fieldVerifiedTarget = (double) averageRemainingToBeTreated + (double) totalTreated;
    meta = "(" + meta + ")" + " + " + "total treated: " + totalTreated;

    return new ColumnData().setValue(fieldVerifiedTarget).setMeta(meta);
  }


  private List<LocationResponse> setGeoJsonProperties(Map<UUID, RowData> rowDataMap,
      List<LocationResponse> locationResponses, String reportLevel) {
    return locationResponses.stream().map(loc -> {
      if (rowDataMap.containsKey(loc.getIdentifier())) {
        loc.getProperties()
            .setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
        if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
            .get(BUSINESS_STATUS) != null) {
          String businessStatus = (String) rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
              .get(BUSINESS_STATUS).getValue();
          loc.getProperties().setBusinessStatus(
              businessStatus);
          loc.getProperties().setStatusColor(getBusinessStatusColor(businessStatus));
        }
      } else {
        return null;
      }
      loc.getProperties().setId(loc.getIdentifier().toString());
      loc.getProperties().setReportLevel(reportLevel);
      return loc;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public FeatureSetResponse getFeatureSetResponse(UUID parentIdentifier,
      List<PlanLocationDetails> locationDetails, Map<UUID, RowData> rowDataMap, String reportLevel,
      MdaLiteReportType type) {

    FeatureSetResponse response = new FeatureSetResponse();
    response.setType("FeatureCollection");
    List<LocationResponse> locationResponses = locationDetails.stream()
        .map(loc -> LocationResponseFactory.fromPlanLocationDetails(loc, parentIdentifier))
        .collect(Collectors.toList());

    locationResponses = setGeoJsonProperties(rowDataMap, locationResponses, reportLevel);

    if (!reportLevel.equals(DashboardService.STRUCTURE_LEVEL)) {
      String defaultColumn = dashboardProperties.getOnchoDefaultDisplayColumns();
      response.setDefaultDisplayColumn(defaultColumn);
    }

    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }


}
