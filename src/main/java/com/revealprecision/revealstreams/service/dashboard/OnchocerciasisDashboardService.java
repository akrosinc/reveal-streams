package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.props.DashboardProperties.SPRAY_COVERAGE_OF_TARGETED;

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

  private static final String OFFICIAL_POP_TARGET = "Official Pop Target";
  private static final String FIELD_VERIFIED_POP_TARGET = "Field Verified Pop Target";
  public static final String TOTAL_TREATED = "Total Treated";
  public static final String OFFICIAL_POP_TREATMENT_COVERAGE = "Official Pop Treatment Coverage";
  public static final String FIELD_VERIFIED_POP_TREATMENT_COVERAGE = "Field Verified Pop Treatment Coverage";
  public static final String TOTAL_STRUCTURE_COUNT = "Total Structure Count";
  public static final String STRUCTURES_COMPLETE = "Structures Complete";
  public static final String STRUCTURES_PARTIALLY_COMPLETE = "Structures Partially Complete";
  public static final String STRUCTURES_REFUSED_ABSENT = "Structures Refused / Absent";
  public static final String STRUCTURES_NOT_YET_VISITED = "Structures Not Visited Yet";
  public static final String COVERAGE_OF_STRUCTURES_VISITED = "Coverage Of Structures Visited";
  public static final String COVERAGE_OF_STRUCTURES_COMPLETED = "Coverage Of Structures Completed";

  public static final String RECEIVED_BY_CDD = "Received by CDD";
  public static final String ADMINISTERED = "Administered";
  public static final String LOST_DAMAGED = "Lost / Damaged";
  public static final String ADVERSE_REACTION = "Adverse reaction";
  public static final String RETURNED_TO_SUPERVISOR = "Returned to supervisor";

  public static final String HOUSEHOLD_DISTRIBUTION = "Household Distribution";
  public static final String POINT_DISTRIBUTION = "Point Distribution";
  public static final String TOTAL_LIVING_ON_THE_STREET = "Total living on the street";

  private static final String MALES_5_14 = "Male 5-14 years";
  private static final String MALES_15 = "Male 15+ years";
  private static final String FEMALES_5_14 = "Female 5-14 years";
  private static final String FEMALES_15 = "Female 15+ years";

  public static final String TOTAL_UNTREATED = "Total Untreated";
  public static final String PREGNANT = "Pregnant";
  public static final String CHILD_UNDER_5 = "Child < 5";
  public static final String SICK = "Sick";
  public static final String ABSENT = "Absent";
  public static final String REFUSAL = "Refusal";

  public static final String PHONE_NUMBER = "Phone Number";
  public static final String NUMBER_OF_STRUCTURES_WITHIN_HOUSEHOLD = "Number of Structures within household";
  public static final String HEAD_OF_HOUSE_HOLD = "Head of Household";


  public static final String ONCHOCERCIASIS = "Onchocerciasis";
  public static final String SCH = "SCH";


  public static final String NTD = "ntd";

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

    Map<String, ColumnData> columns = new HashMap<>();
    if (type == MdaLiteReportType.DRUG_DISTRIBUTION) {
      columns = getDrugDistributionDashboardData(
          onchoSurveyFromDrugAccountability,
          onchoSurveyFromAdverseEventsRecord);
    } else if (type == MdaLiteReportType.TREATMENT_COVERAGE) {
      columns = getTreatmentCoverageDashboardData(onchocerciasisSurvey,
          null,
          plan,
          childLocation,
          locationBusinessStateObjPerGeoLevelMap
      );
    } else if (type == MdaLiteReportType.POPULATION_DISTRIBUTION) {
      columns = getPopulationDistributionDashboardData(onchocerciasisSurvey,
          aggregationDataFromTreatmentOutsideHousehold
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
        dashboardProperties.getMdaLiteSchImportTag());


  }

  private ColumnData getTotalStructuresTargetedCount(Plan plan, Location childLocation,
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

    double totalStructuresInTargetedCount =
        totalStructuresInPlanLocationCount - notEligibleStructuresCount;

    ColumnData totalStructuresTargetedColumnData = new ColumnData();
    totalStructuresTargetedColumnData.setValue(totalStructuresInTargetedCount);
    totalStructuresTargetedColumnData.setIsPercentage(false);
    return totalStructuresTargetedColumnData;
  }

  private Map<String, ColumnData> getPopulationDistributionDashboardData(
      OnchocerciasisSurveyCddSummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      List<OnchocerciasisSurveyCddSummaryAggregationProjection> aggregationDataFromTreatmentOutsideHouseholdList) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    OnchocerciasisSurveyCddSummaryAggregationProjection pointDistributionData = getHomelessOrPointDistributionColumnData(
        aggregationDataFromTreatmentOutsideHouseholdList, "Point Distribution");

    OnchocerciasisSurveyCddSummaryAggregationProjection homelessIndividuals = getHomelessOrPointDistributionColumnData(
        aggregationDataFromTreatmentOutsideHouseholdList,
        "Homeless individuals");

    columns.put(HOUSEHOLD_DISTRIBUTION,
        getHouseholdDistributionColumnData(aggregationDataFromCddSupervisorDailySummary));

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

    columns.put(CHILD_UNDER_5,
        new ColumnData().setValue(
            (homelessIndividuals == null ? 0 : homelessIndividuals.getTotalUntreatedUnderFive()) +
                (pointDistributionData == null ? 0
                    : pointDistributionData.getTotalUntreatedUnderFive()) +
                (aggregationDataFromCddSupervisorDailySummary == null ? 0
                    : aggregationDataFromCddSupervisorDailySummary.getTotalUntreatedUnderFive())));

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

    return columns;
  }


  private Map<String, ColumnData> getOperationalData(
      OnchocerciasisSurveyCddSummaryAggregationProjection onchoSurveyFromHouseholdHeadData) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put("LOCATION",
        new ColumnData().setIsHidden(true).setDataType("string").setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getLocationIdentifier()) ));

    columns.put(HEAD_OF_HOUSE_HOLD,
        new ColumnData().setDataType("string").setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getHouseholdHead()) ));

    columns.put(PHONE_NUMBER,
        new ColumnData().setDataType("string").setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getHouseholdHeadPhoneNumber()) ));

    columns.put(NUMBER_OF_STRUCTURES_WITHIN_HOUSEHOLD,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getNumberOfStructures()) ));

    columns.put(TOTAL_TREATED,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalTreated()) ));

    columns.put(MALES_5_14,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalTreatedMaleFiveFourteen()) ));

    columns.put(FEMALES_5_14,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalTreatedFemaleFiveFourteen()) ));


    columns.put(MALES_15,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalTreatedMaleAboveFifteen()) ));

    columns.put(FEMALES_15,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalTreatedFemaleAboveFifteen()) ));

    columns.put(TOTAL_UNTREATED,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalTreated()) ));

    columns.put(PREGNANT,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalUntreatedPregnant()) ));

    columns.put(CHILD_UNDER_5,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalUntreatedUnderFive()) ));

    columns.put(SICK,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalUntreatedSick()) ));

    columns.put(ABSENT,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalUntreatedAbsent()) ));

    columns.put(REFUSAL,
        new ColumnData().setValue(
            (onchoSurveyFromHouseholdHeadData == null ? 0 : onchoSurveyFromHouseholdHeadData.getTotalUntreatedRefusal()) ));

    columns.put(ADMINISTERED,
        getDummy());

    return columns;
  }


  private Map<String, ColumnData> getDrugDistributionDashboardData(
      OnchocerciasisSurveyDrugAccountabilityAggregationProjection onchoSurveyFromDrugAccountability,
      OnchocerciasisSurveyAdverseEventsAggregationProjection onchoSurveyFromAdverseEventsRecord) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(getColumnName(RECEIVED_BY_CDD),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability==null?0:onchoSurveyFromDrugAccountability.getTabletsReceived()));

    columns.put(getColumnName(ADMINISTERED),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability==null?0:onchoSurveyFromDrugAccountability.getTabletsUsed()));


    columns.put(getColumnName(LOST_DAMAGED),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability==null?0:onchoSurveyFromDrugAccountability.getTabletsLost()));


    columns.put(getColumnName(ADVERSE_REACTION),
        new ColumnData().setValue(onchoSurveyFromAdverseEventsRecord==null?0:onchoSurveyFromAdverseEventsRecord.getReadminstered()));

    columns.put(getColumnName(RETURNED_TO_SUPERVISOR),
        new ColumnData().setValue(onchoSurveyFromDrugAccountability==null?0:onchoSurveyFromDrugAccountability.getTabletsReturned()));

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
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap
  ) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();
    ColumnData treatmentCoverage = getTreatmentCoverage(
        aggregationDataFromCddSupervisorDailySummary, locationMetadataDoubleAggregateProjection);

    columns.put(OFFICIAL_POP_TARGET,
        getCensusPopTarget(locationMetadataDoubleAggregateProjection));

    columns.put(FIELD_VERIFIED_POP_TARGET,
        getFieldVerifiedPopTarget(locationMetadataDoubleAggregateProjection));

    columns.put(TOTAL_TREATED, getTreated(aggregationDataFromCddSupervisorDailySummary));

    columns.put(OFFICIAL_POP_TREATMENT_COVERAGE,
        treatmentCoverage);

    columns.put(FIELD_VERIFIED_POP_TREATMENT_COVERAGE,
        treatmentCoverage);

    ColumnData totalStructuresTargetedCount = getTotalStructuresTargetedCount(plan, location,
        locationBusinessStateObjPerGeoLevelMap);

    columns.put(TOTAL_STRUCTURE_COUNT,
        totalStructuresTargetedCount);

    ColumnData mdaCompleteStructures = getStructureBusinessStatusColumnData(
        locationBusinessStateObjPerGeoLevelMap, BusinessStatus.MDA_COMPLETE);
    columns.put(STRUCTURES_COMPLETE,
        mdaCompleteStructures);

    ColumnData partiallyCompleteStructures = getStructureBusinessStatusColumnData(
        locationBusinessStateObjPerGeoLevelMap, BusinessStatus.MDA_PARTIALLY_COMPLETE);
    columns.put(STRUCTURES_PARTIALLY_COMPLETE,
        partiallyCompleteStructures);

    ColumnData refusedStructures = getStructureBusinessStatusColumnData(
        locationBusinessStateObjPerGeoLevelMap, BusinessStatus.MDA_REFUSED_OR_ABSENT);
    columns.put(STRUCTURES_REFUSED_ABSENT,
        refusedStructures);

    columns.put(STRUCTURES_NOT_YET_VISITED,
        getStructureBusinessStatusColumnData(locationBusinessStateObjPerGeoLevelMap,
            BusinessStatus.NOT_VISITED));

    columns.put(COVERAGE_OF_STRUCTURES_VISITED,
        getDummy());

    columns.put(COVERAGE_OF_STRUCTURES_COMPLETED,
        getDummy());

    return columns;
  }

  public List<RowData> getMDALiteCoverageDataAboveStructureLevel(Plan plan, Location childLocation,
      MdaLiteReportType type,Location parentLocation) {

    List<OnchocerciasisSurveyCddSummaryAggregationProjection> onchoSurveyFromHouseholdHeadDataList = eventTrackerRepository.getOnchoSurveyFromHouseholdHeadData(
        parentLocation.getIdentifier(), plan.getIdentifier());

    List<RowData> collect = onchoSurveyFromHouseholdHeadDataList.stream()
        .map(this::getOperationalData)
        .map(stringColumnDataMap -> {
          RowData rowData = new RowData();
          rowData.setLocationIdentifier(UUID.fromString((String) stringColumnDataMap.get("LOCATION").getValue()));
          rowData.setColumnDataMap(stringColumnDataMap);
          rowData.setLocationName((String) stringColumnDataMap.get(HEAD_OF_HOUSE_HOLD).getValue());
          return rowData;
        })
        .collect(Collectors.toList());
    return collect;
  }



  private ColumnData getTreated(
      OnchocerciasisSurveyCddSummaryAggregationProjection OnchocerciasisSurveyCddSummaryAggregationProjection) {

    if (OnchocerciasisSurveyCddSummaryAggregationProjection != null) {
      return new ColumnData().setValue(
          OnchocerciasisSurveyCddSummaryAggregationProjection.getTotalTreated());
    } else {
      return new ColumnData().setValue(0);
    }
  }


  private ColumnData getDummy() {
    return new ColumnData().setValue(0);
  }

  private ColumnData getHouseholdDistributionColumnData(
      OnchocerciasisSurveyCddSummaryAggregationProjection onchocerciasisSurveyCddSummaryAggregationProjection) {
    if (onchocerciasisSurveyCddSummaryAggregationProjection == null) {
      return null;
    } else {
      return new ColumnData().setValue(onchocerciasisSurveyCddSummaryAggregationProjection.getTotalTreated());
    }
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
      Map<String, LocationBusinessStateCount> locationBusinessStateObjPerGeoLevelMap,
      String businessStatus) {

    return new ColumnData().setValue(
        locationBusinessStateObjPerGeoLevelMap.get(businessStatus).getLocationCount());
  }



  private ColumnData getHiddenColumn(Object value) {
    return new ColumnData().setIsHidden(true).setValue(value);
  }

  private ColumnData getTreatmentCoverage(
      OnchocerciasisSurveyCddSummaryAggregationProjection OnchocerciasisSurveyCddSummaryAggregationProjection,
      LocationMetadataDoubleAggregateProjection locationMetadataDoubleAggregateProjection) {

    ColumnData censusPopTarget = getCensusPopTarget(locationMetadataDoubleAggregateProjection);

    ColumnData treated = getTreated(
        OnchocerciasisSurveyCddSummaryAggregationProjection);

    if (censusPopTarget.getValue() != null && ((Integer) censusPopTarget.getValue()) > 0) {
      if (treated.getValue() != null && ((Integer) treated.getValue()) > 0) {

        Double treatmentCoverage = ((Integer) treated.getValue()).doubleValue()
            / ((Integer) censusPopTarget.getValue()).doubleValue() * 100;
        String meta = "Treated: " + treated.getValue() + " / " + "Census Target " + ": "
            + censusPopTarget.getValue();
        return new ColumnData().setValue(treatmentCoverage).setMeta(meta).setIsPercentage(true)
            .setDataType("double");
      }
    }
    return new ColumnData().setValue(0);
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
      LocationMetadataDoubleAggregateProjection locationMetadataDoubleAggregateProjection) {

    int sum = 0;
    if (locationMetadataDoubleAggregateProjection != null) {
      sum = locationMetadataDoubleAggregateProjection.getValue();
    }

    return new ColumnData().setValue(sum);
  }





  private List<LocationResponse> setGeoJsonProperties(Map<UUID, RowData> rowDataMap,
      List<LocationResponse> locationResponses, String reportLevel) {
    return locationResponses.stream().map(loc -> {
      if (rowDataMap.containsKey(loc.getIdentifier())) {
        loc.getProperties()
            .setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
        if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(SPRAY_COVERAGE_OF_TARGETED)
            != null) {
          loc.getProperties().setSprayCoverage(
              rowDataMap.get(loc.getIdentifier()).getColumnDataMap().get(SPRAY_COVERAGE_OF_TARGETED)
                  .getValue());
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

    String defaultColumn = dashboardProperties.getMdaLiteDefaultDisplayColumnsWithType()
        .getOrDefault(type.name(), null);

    response.setDefaultDisplayColumn(defaultColumn);
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }


}
