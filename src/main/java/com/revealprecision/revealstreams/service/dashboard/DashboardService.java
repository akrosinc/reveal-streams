package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.enums.ActionTitleEnum.MDA_ONCHOCERCIASIS_SURVEY;
import static com.revealprecision.revealstreams.enums.ReportTypeEnum.ONCHOCERCIASIS_SURVEY;

import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.LookupUtil;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.ApplicableReportsEnum;
import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.enums.ReportTypeEnum;
import com.revealprecision.revealstreams.exceptions.WrongEnumException;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Action;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.service.LocationService;
import com.revealprecision.revealstreams.service.PlanService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DashboardService {

  private final LocationService locationService;
  private final PlanService planService;
  private final MDADashboardService mdaDashboardService;
  private final IRSDashboardService irsDashboardService;
  private final IRSLiteDashboardService irsLiteDashboardService;
  private final MDALiteDashboardService mdaLiteDashboardService;
  private final SurveyDashboardService surveyDashboardService;
  private final LsmDashboardService lsmDashboardService;
  private final OnchocerciasisDashboardService onchocerciasisDashboardService;

  public static final String WITHIN_STRUCTURE_LEVEL = "Within Structure";
  public static final String STRUCTURE_LEVEL = "Structure";
  public static final String DIRECTLY_ABOVE_STRUCTURE_LEVEL = "Directly Above Structure";
  public static final String ALL_OTHER_LEVELS = "All Other Levels";
  public static final String LOWEST_LITE_TOUCH_LEVEL = "Lowest Lite Touch Level";
  public static final String IS_ON_PLAN_TARGET = "In On Plan Target";
  public static final String SUPERVISOR_LEVEL = "Supervisor Level";
  public static final String CDD_LEVEL = "CDD Level";

  public FeatureSetResponse getDataForReport(String reportType, UUID planIdentifier,
      String parentIdentifierString, List<String> filters, MdaLiteReportType type) {

    ReportTypeEnum reportTypeEnum = LookupUtil.lookup(ReportTypeEnum.class, reportType);
    Plan plan = planService.findPlanByIdentifier(planIdentifier);
    checkSupportedReports(reportType, planIdentifier, reportTypeEnum, plan);
    Location parentLocation = null;
    UUID parentIdentifier = null;

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
        planIdentifier, parentIdentifier, plan, parentLocation);

    try {
      initialDataStores(reportTypeEnum);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String reportLevel = getReportLevel(plan, parentLocation, parentIdentifierString);

    reportTypeEnum = getReportTypeEnum(plan, reportTypeEnum);

    ReportTypeEnum finalReportTypeEnum = reportTypeEnum;
    Map<UUID, RowData> rowDataMap = locationDetails.stream().flatMap(loc -> Objects.requireNonNull(
                getRowData(loc.getParentLocation(), finalReportTypeEnum, plan, loc, reportLevel, filters,
                    parentIdentifierString, type))
            .stream()).filter(Objects::nonNull)
        .collect(Collectors.toMap(RowData::getLocationIdentifier, row -> row, (a, b) -> b));

    return getFeatureSetResponse(parentIdentifier, locationDetails,
        rowDataMap, reportLevel,
        reportTypeEnum, filters, type);
  }

  private ReportTypeEnum getReportTypeEnum(Plan plan, ReportTypeEnum reportTypeEnum) {

    if (plan.getGoals() != null && !plan.getGoals().isEmpty() && plan.getGoals().stream()
        .flatMap(goal -> goal.getActions().stream()).noneMatch(Objects::nonNull)) {
      return null;
    }
    Optional<Action> hasOnchoAction = plan.getGoals().stream()
        .flatMap(goal -> goal.getActions().stream())
        .filter(action -> action.getTitle().equals(
            MDA_ONCHOCERCIASIS_SURVEY.getActionTitle())).findFirst();
    if (hasOnchoAction.isPresent()) {
      return ONCHOCERCIASIS_SURVEY;
    } else {
      return reportTypeEnum;
    }
  }

  private void checkSupportedReports(String reportType, UUID planIdentifier,
      ReportTypeEnum reportTypeEnum,
      Plan plan) {
    List<String> applicableReportTypes = ApplicableReportsEnum.valueOf(
        plan.getInterventionType().getCode()).getReportName();
    if (!applicableReportTypes.contains(reportTypeEnum.name())) {
      throw new WrongEnumException(
          "Report type: '" + reportType + "' is not applicable to plan with identifier: '"
              + planIdentifier + "'");
    }
  }

  private List<RowData> getRowData(Location parentLocation, ReportTypeEnum reportTypeEnum,
      Plan plan,
      PlanLocationDetails loc, String reportLevel, List<String> filters,
      String parentIdentifierString, MdaLiteReportType type) {

    switch (reportTypeEnum) {
      case MDA_FULL_COVERAGE:

        switch (reportLevel) {
          case WITHIN_STRUCTURE_LEVEL:
            return mdaDashboardService.getMDAFullWithinStructureLevelData(plan, parentLocation);

          case STRUCTURE_LEVEL:
            return mdaDashboardService.getMDAFullCoverageStructureLevelData(plan,
                loc.getLocation(),
                parentLocation.getIdentifier());

          case DIRECTLY_ABOVE_STRUCTURE_LEVEL:
            return mdaDashboardService.getMDAFullCoverageOperationalAreaLevelData(plan,
                loc.getLocation());

          case ALL_OTHER_LEVELS:
            return mdaDashboardService.getMDAFullCoverageData(plan, loc.getLocation());
        }

      case IRS_FULL_COVERAGE:

        switch (reportLevel) {
          case WITHIN_STRUCTURE_LEVEL:
          case STRUCTURE_LEVEL:
            return irsDashboardService.getIRSFullCoverageStructureLevelData(plan,
                loc.getLocation());

          case DIRECTLY_ABOVE_STRUCTURE_LEVEL:
            return irsDashboardService.getIRSFullDataOperational(plan,
                loc.getLocation());

          case ALL_OTHER_LEVELS:
            return irsDashboardService.getIRSFullData(plan, loc.getLocation());
        }
      case SURVEY:

        switch (reportLevel) {
          case WITHIN_STRUCTURE_LEVEL:
          case STRUCTURE_LEVEL:
            return surveyDashboardService.getIRSFullCoverageStructureLevelData(plan,
                loc.getLocation());
          case DIRECTLY_ABOVE_STRUCTURE_LEVEL:
          case ALL_OTHER_LEVELS:
            return surveyDashboardService.getIRSFullData(plan, loc.getLocation());
        }
      case IRS_LITE_COVERAGE:
        switch (reportLevel) {
          case DIRECTLY_ABOVE_STRUCTURE_LEVEL:
          case LOWEST_LITE_TOUCH_LEVEL:
          case IS_ON_PLAN_TARGET:
            return irsLiteDashboardService.getIRSFullDataOperational(plan,
                loc.getLocation());

          case ALL_OTHER_LEVELS:
            return irsLiteDashboardService.getIRSFullData(plan, loc.getLocation());
        }
      case MDA_LITE_COVERAGE:
        switch (reportLevel) {
          case CDD_LEVEL:
          case SUPERVISOR_LEVEL:
          case IS_ON_PLAN_TARGET:
            return mdaLiteDashboardService.getMDALiteCoverageDataOnTargetLevel(
                plan,
                loc.getLocation(), filters, type);
          case LOWEST_LITE_TOUCH_LEVEL:
          case ALL_OTHER_LEVELS:
            return mdaLiteDashboardService.getMDALiteCoverageData(
                plan,
                loc.getLocation(), filters, type);

        }
      case LSM_HABITAT_SURVEY:
        switch (reportLevel) {
          case WITHIN_STRUCTURE_LEVEL:
          case STRUCTURE_LEVEL:
            return lsmDashboardService.getIRSFullCoverageStructureLevelData(plan,
                loc.getLocation());
          case DIRECTLY_ABOVE_STRUCTURE_LEVEL:
          case ALL_OTHER_LEVELS:
            return lsmDashboardService.getIRSFullData(plan, loc.getLocation(), reportTypeEnum);
        }
      case LSM_HOUSEHOLD_SURVEY:
        switch (reportLevel) {
          case WITHIN_STRUCTURE_LEVEL:
          case STRUCTURE_LEVEL:
            return lsmDashboardService.getIRSFullCoverageStructureLevelData(plan,
                loc.getLocation());
          case DIRECTLY_ABOVE_STRUCTURE_LEVEL:
          case ALL_OTHER_LEVELS:
            return lsmDashboardService.getIRSFullData(plan, loc.getLocation(), reportTypeEnum);
        }
      case ONCHOCERCIASIS_SURVEY:
        switch (reportLevel) {
          case CDD_LEVEL:
          case SUPERVISOR_LEVEL:
          case IS_ON_PLAN_TARGET:
          case STRUCTURE_LEVEL:
          case WITHIN_STRUCTURE_LEVEL:

            return onchocerciasisDashboardService.getMDALiteCoverageDataAboveStructureLevel(
                plan,
                loc.getLocation(), type, parentLocation);
          case LOWEST_LITE_TOUCH_LEVEL:

          case DIRECTLY_ABOVE_STRUCTURE_LEVEL:
            if (type.equals(MdaLiteReportType.DRUG_DISTRIBUTION)) {
              return new ArrayList<>();
            }
            return onchocerciasisDashboardService.getMDALiteCoverageData(
                plan,
                loc.getLocation(), type);
          case ALL_OTHER_LEVELS:
            return onchocerciasisDashboardService.getMDALiteCoverageData(
                plan,
                loc.getLocation(), type);
        }

    }
    return null;
  }

  public FeatureSetResponse getFeatureSetResponse(UUID parentIdentifier,
      List<PlanLocationDetails> locationDetails,
      Map<UUID, RowData> rowDataMap, String reportLevel, ReportTypeEnum reportTypeEnum,
      List<String> filters, MdaLiteReportType type) {
    switch (reportTypeEnum) {

      case MDA_FULL_COVERAGE:
        return mdaDashboardService.getFeatureSetResponse(parentIdentifier, locationDetails,
            rowDataMap, reportLevel);

      case IRS_FULL_COVERAGE:
        return irsDashboardService.getFeatureSetResponse(parentIdentifier, locationDetails,
            rowDataMap, reportLevel);

      case IRS_LITE_COVERAGE:
        return irsLiteDashboardService.getFeatureSetResponse(parentIdentifier, locationDetails,
            rowDataMap, reportLevel);
      case MDA_LITE_COVERAGE:
        return mdaLiteDashboardService.getFeatureSetResponse(parentIdentifier, locationDetails,
            rowDataMap, reportLevel, filters, type);
      case SURVEY:
        return surveyDashboardService.getFeatureSetResponse(parentIdentifier, locationDetails,
            rowDataMap, reportLevel);
      case LSM_HABITAT_SURVEY:
      case LSM_HOUSEHOLD_SURVEY:
        return lsmDashboardService.getFeatureSetResponse(parentIdentifier, locationDetails,
            rowDataMap, reportLevel);
      case ONCHOCERCIASIS_SURVEY:
        return onchocerciasisDashboardService.getFeatureSetResponse(parentIdentifier,
            locationDetails,
            rowDataMap, reportLevel, type);


    }
    return null;
  }

  private void initialDataStores(ReportTypeEnum reportTypeEnum) throws InterruptedException {
    switch (reportTypeEnum) {
      case MDA_FULL_COVERAGE:
        mdaDashboardService.initDataStoresIfNecessary();
        break;
      case IRS_FULL_COVERAGE:
        irsDashboardService.initDataStoresIfNecessary();
        break;
      case IRS_LITE_COVERAGE:
        irsLiteDashboardService.initDataStoresIfNecessary();
        break;
      case MDA_LITE_COVERAGE:
        break;
    }
  }

  private List<PlanLocationDetails> getPlanLocationDetails(UUID planIdentifier,
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
}
