package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.props.DashboardProperties.SPRAY_COVERAGE_OF_TARGETED;

import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.factory.LocationResponseFactory;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.projection.CddDrugReceivedAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.CddDrugWithdrawalAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.CddSupervisorDailySummaryAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.TabletAccountabilityAggregationProjection;
import com.revealprecision.revealstreams.persistence.repository.EventTrackerRepository;
import com.revealprecision.revealstreams.persistence.repository.ReportRepository;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.service.LocationBusinessStatusService;
import com.revealprecision.revealstreams.service.MetadataService;
import com.revealprecision.revealstreams.service.PlanLocationsService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
public class MDALiteDashboardService {

  private static final String SCH_CENSUS_POP_TARGET = "SCH Census Pop Target";
  private static final String STH_CENSUS_POP_TARGET = "STH Census Pop Target";
  public static final String SCH_TREATMENT_COVERAGE = "SCH Treatment Coverage";
  public static final String STH_TREATMENT_COVERAGE = "STH Treatment Coverage";

  public static final String STH_TOTAL_TREATED = "Total Treated (STH)";
  public static final String SCH_TOTAL_TREATED = "Total Treated (SCH)";

  public static final String ADMINISTERED = "Administered";
  public static final String DAMAGED = "Damaged";
  public static final String ADVERSE_REACTION = "Adverse reaction";
  public static final String RETURNED_TO_SUPERVISOR = "Returned to supervisor";
  public static final String SUPERVISOR_DISTRIBUTED = "Supervisor Distributed";
  public static final String WITHDRAWN_FROM_CDD_FOR_DISTRIBUTION = "Withdrawn from CDD for redistribution";
  public static final String RECEIVED_BY_CDD = "Received by CDD";
  public static final String REMAINING_WITH_CDD = "Remaining with CDD";
  public static final String TOTAL_LIVING_ON_THE_STREET = "Total living on the street";
  public static final String TOTAL_LIVING_WITH_DISABILITY = "Total living with disability";
  public static final String TOTAL_BITTEN_BY_SNAKE = "Total bitten by snake";
  public static final String TOTAL_VISITED_HEALTH_FACILITY_AFTER_SNAKE_BITE = "Total visited health facility after snake bite";
  public static final String PERCENTAGE_VISITED_HEALTH_FACILITY_AFTER_SNAKE_BITE = "Percentage visited health facility after snake bite";

  public static final String STH = "STH";
  public static final String SCH = "SCH";

  public static final String DRUG = "drug";

  public static final String NTD = "ntd";

  private final PlanLocationsService planLocationsService;
  private final LocationBusinessStatusService locationBusinessStatusService;
  private final DashboardProperties dashboardProperties;
  private final MetadataService metadataService;
  private final EventTrackerRepository eventTrackerRepository;


  private final ReportRepository planReportRepository;

  boolean datastoresInitialized = false;


  public List<RowData> getMDALiteCoverageData(Plan plan, Location childLocation,
      List<String> filters, MdaLiteReportType type) {

    CddSupervisorDailySummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary = eventTrackerRepository.getAggregationDataFromCddSupervisorDailySummary(
        childLocation.getIdentifier(), filters.get(0), plan.getIdentifier());

    TabletAccountabilityAggregationProjection tabletAccountabilityAggregationProjection = eventTrackerRepository.getAggregationDataFromTabletAccountability(
        childLocation.getIdentifier(), plan.getIdentifier());

    CddDrugReceivedAggregationProjection cddDrugReceivedAggregationProjection = eventTrackerRepository.getAggregationDataFromCddDrugReceived(
        childLocation.getIdentifier(), plan.getIdentifier());

    CddDrugWithdrawalAggregationProjection cddDrugWithdrawalAggregationProjection = eventTrackerRepository.getAggregationDataFromCddDrugWithdrawal(
        childLocation.getIdentifier(), plan.getIdentifier());

    Map<String, ColumnData> columns = new HashMap<>();
    if (type == MdaLiteReportType.DRUG_DISTRIBUTION) {
      columns = getDrugDistributionDashboardData(aggregationDataFromCddSupervisorDailySummary,
          tabletAccountabilityAggregationProjection, cddDrugReceivedAggregationProjection,
          cddDrugWithdrawalAggregationProjection, filters.get(0));
    } else if (type == MdaLiteReportType.TREATMENT_COVERAGE) {
      columns = getTreatmentCoverageDashboardData(aggregationDataFromCddSupervisorDailySummary,
          filters.get(0));
    } else if (type == MdaLiteReportType.POPULATION_DISTRIBUTION) {
      columns = getPopulationDistributionDashboardData(aggregationDataFromCddSupervisorDailySummary,
          filters.get(0));
    }

    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  private Map<String, ColumnData> getPopulationDistributionDashboardData(
      CddSupervisorDailySummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      String filter) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    if (filter.equals(STH)) {
      columns.put(TOTAL_LIVING_ON_THE_STREET,
          getTotalLivingOnTheStreet(aggregationDataFromCddSupervisorDailySummary));

      columns.put(TOTAL_LIVING_WITH_DISABILITY,
          getTotalLivingWithDisability(aggregationDataFromCddSupervisorDailySummary));

      columns.put(TOTAL_BITTEN_BY_SNAKE,
          getTotalBittenBySnake(aggregationDataFromCddSupervisorDailySummary));

      columns.put(TOTAL_VISITED_HEALTH_FACILITY_AFTER_SNAKE_BITE,
          getTotalVisitedHealthFacilityAfterSnakeBite(
              aggregationDataFromCddSupervisorDailySummary));

      columns.put(PERCENTAGE_VISITED_HEALTH_FACILITY_AFTER_SNAKE_BITE,
          getPercentageVisitedHealthFacilityAfterSnakeBite(
              aggregationDataFromCddSupervisorDailySummary));
    }
    return columns;
  }

  private Map<String, ColumnData> getDrugDistributionDashboardData(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection,
      TabletAccountabilityAggregationProjection tabletAccountabilityAggregationProjection,
      CddDrugReceivedAggregationProjection cddDrugReceivedAggregationProjection,
      CddDrugWithdrawalAggregationProjection cddDrugWithdrawalAggregationProjection,
      String filter) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();

    columns.put(getColumnName(SUPERVISOR_DISTRIBUTED, filter),
        getSupervisorDistributed(cddDrugReceivedAggregationProjection, filter));

    columns.put(getColumnName(WITHDRAWN_FROM_CDD_FOR_DISTRIBUTION, filter),
        getWithdrawnFromCdd(cddDrugWithdrawalAggregationProjection, filter));

    columns.put(getColumnName(RECEIVED_BY_CDD, filter),
        getReceivedByCdd(cddDrugWithdrawalAggregationProjection,
            cddDrugReceivedAggregationProjection, filter));

    columns.put(getColumnName(ADMINISTERED, filter),
        getAdministered(cddSupervisorDailySummaryAggregationProjection));

    columns.put(getColumnName(DAMAGED, filter),
        getDamaged(cddSupervisorDailySummaryAggregationProjection));

    columns.put(getColumnName(REMAINING_WITH_CDD, filter),
        getRemainingWithCDD(cddSupervisorDailySummaryAggregationProjection,
            cddDrugWithdrawalAggregationProjection, cddDrugReceivedAggregationProjection, filter));

    columns.put(getColumnName(RETURNED_TO_SUPERVISOR, filter),
        getReturned(tabletAccountabilityAggregationProjection, filter));

    columns.put(getColumnName(ADVERSE_REACTION, filter),
        getAdverseReaction(cddSupervisorDailySummaryAggregationProjection));

    return columns;
  }

  private String getColumnName(String column, String filter) {
    return column.concat("(").concat(filter).concat(")");
  }

  private Map<String, ColumnData> getTreatmentCoverageDashboardData(
      CddSupervisorDailySummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary,
      String filter) {
    Map<String, ColumnData> columns = new LinkedHashMap<>();
    if (filter.equals(SCH)) {
      columns.put(SCH_TOTAL_TREATED, getTreated(aggregationDataFromCddSupervisorDailySummary));
    } else {
      columns.put(STH_TOTAL_TREATED, getTreated(aggregationDataFromCddSupervisorDailySummary));
    }
    return columns;
  }

  public List<RowData> getMDALiteCoverageDataOnTargetLevel(Plan plan, Location childLocation,
      List<String> filters, MdaLiteReportType type) {
    CddSupervisorDailySummaryAggregationProjection aggregationDataFromCddSupervisorDailySummary = eventTrackerRepository.getAggregationDataFromCddSupervisorDailSummaryOnPlanTarget(
        childLocation.getIdentifier(), "STH", plan.getIdentifier());

    TabletAccountabilityAggregationProjection tabletAccountabilityAggregationProjection = eventTrackerRepository.getAggregationDataFromTabletAccountabilityOnPlanTarget(
        childLocation.getIdentifier(), plan.getIdentifier());

    CddDrugReceivedAggregationProjection cddDrugReceivedAggregationProjection = eventTrackerRepository.getAggregationDataFromCddDrugReceivedOnPlanTarget(
        childLocation.getIdentifier(), plan.getIdentifier());

    CddDrugWithdrawalAggregationProjection cddDrugWithdrawalAggregationProjection = eventTrackerRepository.getAggregationDataFromCddDrugWithdrawalOnPlanTarget(
        childLocation.getIdentifier(), plan.getIdentifier());

    Map<String, ColumnData> columns = new HashMap<>();
    if (type == MdaLiteReportType.DRUG_DISTRIBUTION) {
      columns = getDrugDistributionDashboardData(aggregationDataFromCddSupervisorDailySummary,
          tabletAccountabilityAggregationProjection, cddDrugReceivedAggregationProjection,
          cddDrugWithdrawalAggregationProjection, filters.get(0));
    } else if (type == MdaLiteReportType.TREATMENT_COVERAGE) {
      columns = getTreatmentCoverageDashboardData(aggregationDataFromCddSupervisorDailySummary,
          filters.get(0));
    } else if (type == MdaLiteReportType.POPULATION_DISTRIBUTION) {
      columns = getPopulationDistributionDashboardData(aggregationDataFromCddSupervisorDailySummary,
          filters.get(0));
    }

//    columns.put(TARGET_SPRAY_AREAS, getTargetedAreas(plan, childLocation));
//    columns.put(VISITED_AREAS, operationalAreaVisitedCounts(plan, childLocation));
//    columns.put(STRUCTURES_ON_THE_GROUND, getTotalStructuresCounts(plan, childLocation));
//    columns.put(STRUCTURES_SPRAYED, getTotalStructuresSprayed(plan,
//        childLocation));
//    columns.put(SPRAY_PROGRESS_SPRAYED_TARGETED,
//        getSPrayedProgressTargeted(plan, childLocation));
//    columns.put(STRUCTURES_FOUND, getTotalStructuresFoundCount(plan, childLocation));
//    columns.put(SPRAY_COVERAGE_OF_FOUND, getSprayCoverageOfFound(plan, childLocation));
//    columns.put(NUMBER_OF_SPRAY_DAYS,
//        getNumberOfSprayDays(report));
//    columns.put(TOTAL_SUPERVISOR_FORMS_SUBMITTED,
//        getSupervisorFormSubmissions(report));
//    columns.put(AVERAGE_STRUCTURES_PER_DAY,
//        getAverageStructuresSprayedPerDay(plan, childLocation, report));
//    columns.put(AVERAGE_INSECTICIDE_USAGE_RATE,
//        getAverageInsecticideUsage(plan, childLocation, report));

    RowData rowData = new RowData();
    rowData.setLocationIdentifier(childLocation.getIdentifier());
    rowData.setColumnDataMap(columns);
    rowData.setLocationName(childLocation.getName());
    return List.of(rowData);
  }

  private ColumnData getTotalLivingOnTheStreet(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(
          cddSupervisorDailySummaryAggregationProjection.getTotalLivingOnTheStreet());
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getTreated(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(
          cddSupervisorDailySummaryAggregationProjection.getTotalTreated());
    } else {
      return new ColumnData().setValue(0);
    }

  }

  private ColumnData getAdministered(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(
          cddSupervisorDailySummaryAggregationProjection.getAdministered());
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getDamaged(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(cddSupervisorDailySummaryAggregationProjection.getDamaged());
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getAdverseReaction(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(cddSupervisorDailySummaryAggregationProjection.getAdverse());
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getReturned(
      TabletAccountabilityAggregationProjection tabletAccountabilityAggregationProjection,
      String filter) {

    if (tabletAccountabilityAggregationProjection != null) {
      int returned = 0;
      if (filter.equals(STH)) {
        returned = tabletAccountabilityAggregationProjection.getMbzReturned();
      } else {
        returned = tabletAccountabilityAggregationProjection.getPzqReturned();
      }
      return new ColumnData().setValue(returned);
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getRemainingWithCDD(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection,
      CddDrugWithdrawalAggregationProjection cddDrugWithdrawalAggregationProjection,
      CddDrugReceivedAggregationProjection cddDrugReceivedAggregationProjection, String filter) {

    ColumnData administered = getAdministered(cddSupervisorDailySummaryAggregationProjection);
    ColumnData damaged = getDamaged(cddSupervisorDailySummaryAggregationProjection);

    ColumnData receivedByCdd = getReceivedByCdd(cddDrugWithdrawalAggregationProjection,
        cddDrugReceivedAggregationProjection, filter);

    Integer remainingWithCdd = (
        ((Integer) (receivedByCdd.getValue() == null ? 0 : receivedByCdd.getValue())) - (
            ((Integer) (administered.getValue() == null ? 0 : administered.getValue()))
                + ((Integer) (damaged.getValue() == null ? 0 : damaged.getValue()))));

    return new ColumnData().setValue(remainingWithCdd);
  }

  private ColumnData getSupervisorDistributed(
      CddDrugReceivedAggregationProjection cddDrugReceivedAggregationProjection, String filter) {

    if (cddDrugReceivedAggregationProjection != null) {
      int returned = 0;
      if (filter.equals(STH)) {
        returned = cddDrugReceivedAggregationProjection.getMbzReceived();
      } else {
        returned = cddDrugReceivedAggregationProjection.getPzqReceived();
      }
      return new ColumnData().setValue(returned);
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getWithdrawnFromCdd(
      CddDrugWithdrawalAggregationProjection cddDrugWithdrawalAggregationProjection,
      String filter) {

    if (cddDrugWithdrawalAggregationProjection != null) {
      int returned = 0;
      if (filter.equals(STH)) {
        returned = cddDrugWithdrawalAggregationProjection.getMbzWithdrawn();
      } else {
        returned = cddDrugWithdrawalAggregationProjection.getPzqWithdrawn();
      }
      return new ColumnData().setValue(returned);
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getReceivedByCdd(
      CddDrugWithdrawalAggregationProjection cddDrugWithdrawalAggregationProjection,
      CddDrugReceivedAggregationProjection cddDrugReceivedAggregationProjection, String filter) {

    Integer receive = null;
    if (cddDrugReceivedAggregationProjection != null) {

      if (filter.equals(STH)) {
        receive = cddDrugReceivedAggregationProjection.getMbzReceived();
      } else {
        receive = cddDrugReceivedAggregationProjection.getPzqReceived();
      }
    }

    Integer withdrawn = null;
    if (cddDrugWithdrawalAggregationProjection != null) {

      if (filter.equals(STH)) {
        withdrawn = cddDrugWithdrawalAggregationProjection.getMbzWithdrawn();
      } else {
        withdrawn = cddDrugWithdrawalAggregationProjection.getPzqWithdrawn();
      }
    }

    Integer receivedByCdd = (receive == null ? 0 : receive) - (withdrawn == null ? 0 : withdrawn);
    return new ColumnData().setValue(receivedByCdd);
  }

  private ColumnData getTotalLivingWithDisability(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(
          cddSupervisorDailySummaryAggregationProjection.getTotalPeopleLivingWithDisability());
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getTotalBittenBySnake(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(
          cddSupervisorDailySummaryAggregationProjection.getTotalBittenBySnake());
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getTotalVisitedHealthFacilityAfterSnakeBite(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      return new ColumnData().setValue(
          cddSupervisorDailySummaryAggregationProjection.getTotalVisitedHealthFacilityAfterSnakeBite());
    } else {
      return new ColumnData().setValue(0);
    }
  }

  private ColumnData getPercentageVisitedHealthFacilityAfterSnakeBite(
      CddSupervisorDailySummaryAggregationProjection cddSupervisorDailySummaryAggregationProjection) {

    if (cddSupervisorDailySummaryAggregationProjection != null) {
      int totalVisitedHealthFacilityAfterSnakeBite = cddSupervisorDailySummaryAggregationProjection.getTotalVisitedHealthFacilityAfterSnakeBite();

      int totalBittenBySnake = cddSupervisorDailySummaryAggregationProjection.getTotalBittenBySnake();

      if (totalBittenBySnake > 0) {
        return new ColumnData().setValue(
                (double) totalVisitedHealthFacilityAfterSnakeBite / (double) totalBittenBySnake * 100)
            .setIsPercentage(true).setMeta("Total visited health facility after snake bite: "
                + totalVisitedHealthFacilityAfterSnakeBite + " / " + "Total bitten by snake: "
                + totalBittenBySnake);
      }
      return new ColumnData().setValue(0);
    } else {
      return new ColumnData().setValue(0);
    }
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
//      if (rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
//          .get(LOCATION_STATUS) != null) {
//        String businessStatus = (String) rowDataMap.get(loc.getIdentifier()).getColumnDataMap()
//            .get(LOCATION_STATUS).getValue();
//        loc.getProperties().setBusinessStatus(
//            businessStatus);
//        loc.getProperties().setStatusColor(getBusinessStatusColor(businessStatus));
//      }
    }).collect(Collectors.toList());
  }

  public FeatureSetResponse getFeatureSetResponse(UUID parentIdentifier,
      List<PlanLocationDetails> locationDetails, Map<UUID, RowData> rowDataMap, String reportLevel,
      List<String> filters, MdaLiteReportType type) {

    FeatureSetResponse response = new FeatureSetResponse();
    response.setType("FeatureCollection");
    List<LocationResponse> locationResponses = locationDetails.stream()
        .map(loc -> LocationResponseFactory.fromPlanLocationDetails(loc, parentIdentifier))
        .collect(Collectors.toList());

    locationResponses = setGeoJsonProperties(rowDataMap, locationResponses);

    String defaultColumn = dashboardProperties.getMdaLiteDefaultDisplayColumnsWithType()
        .getOrDefault(type.name().concat(filters.get(0)), null);

    response.setDefaultDisplayColumn(defaultColumn);
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    return response;
  }


}
