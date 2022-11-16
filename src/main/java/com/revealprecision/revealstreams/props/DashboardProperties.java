package com.revealprecision.revealstreams.props;

import static com.revealprecision.revealstreams.service.dashboard.DashboardService.ALL_OTHER_LEVELS;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.CDD_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.DIRECTLY_ABOVE_STRUCTURE_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.IS_ON_PLAN_TARGET;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.STRUCTURE_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.DashboardService.SUPERVISOR_LEVEL;
import static com.revealprecision.revealstreams.service.dashboard.IRSLiteDashboardService.SPRAY_PROGRESS_SPRAYED_TARGETED;
import static com.revealprecision.revealstreams.service.dashboard.LsmDashboardService.SURVEY_COVERAGE;
import static com.revealprecision.revealstreams.service.dashboard.MDADashboardService.DISTRIBUTION_COVERAGE;
import static com.revealprecision.revealstreams.service.dashboard.MDADashboardService.DISTRIBUTION_COVERAGE_PERCENTAGE;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardOldService.ADVERSE;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardOldService.ALB;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardOldService.MALES_1_4;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardOldService.MZB;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardOldService.PZQ;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardOldService.SCH_TREATMENT_COVERAGE;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardOldService.STH_TREATMENT_COVERAGE;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardService.ADVERSE_REACTION;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardService.NTD;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardService.PERCENTAGE_VISITED_HEALTH_FACILITY_AFTER_SNAKE_BITE;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardService.SCH;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardService.STH;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardService.STH_TOTAL_TREATED;
import static com.revealprecision.revealstreams.service.dashboard.MDALiteDashboardService.TOTAL_LIVING_ON_THE_STREET;
import static com.revealprecision.revealstreams.service.dashboard.SurveyDashboardService.VISITATION_COVERAGE;
import static java.util.Map.entry;

import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.enums.PlanInterventionTypeEnum;
import com.revealprecision.revealstreams.enums.ReportTypeEnum;
import com.revealprecision.revealstreams.service.dashboard.IrsLitePerformanceDashboardService;
import com.revealprecision.revealstreams.service.dashboard.IrsPerformanceDashboardService;
import com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "dashboard")
@Component
@Setter
@Getter
public class DashboardProperties {

  public static final String SPRAY_COVERAGE_OF_TARGETED = "Spray Progress (Sprayed/Targeted)";

  private List<String> dashboards = List.of("IRS_COVERAGE", "MDA_COVERAGE");

  private Map<String, Map<String, ColumnMeta>> dashboardColumns = Map.of("MDA_COVERAGE",
      Map.of("TOTAL_STRUCTURES", new ColumnMeta("Total Structures", false),
          "TOTAL_STRUCTURES_FOUND", new ColumnMeta("Total Structures Found", false),
          "FOUND_COVERAGE", new ColumnMeta("Found Coverage", true), "OPERATIONAL_AREA_VISITED",
          new ColumnMeta("Operational Area Visited", false)));

  private final Map<String, String> mdaDefaultDisplayColumns = Map.of(
      DIRECTLY_ABOVE_STRUCTURE_LEVEL, DISTRIBUTION_COVERAGE_PERCENTAGE, ALL_OTHER_LEVELS,
      DISTRIBUTION_COVERAGE);

  private final Map<String, String> irsLiteDefaultDisplayColumns = Map.of(
      DIRECTLY_ABOVE_STRUCTURE_LEVEL, SPRAY_PROGRESS_SPRAYED_TARGETED, ALL_OTHER_LEVELS,
      SPRAY_PROGRESS_SPRAYED_TARGETED);

  private final Map<String, String> irsDefaultDisplayColumns = Map.of(
      DIRECTLY_ABOVE_STRUCTURE_LEVEL, SPRAY_COVERAGE_OF_TARGETED, ALL_OTHER_LEVELS,
      SPRAY_COVERAGE_OF_TARGETED);

  private final Map<String, String> surveyDefaultDisplayColumns = Map.of(
      ALL_OTHER_LEVELS,
      VISITATION_COVERAGE,DIRECTLY_ABOVE_STRUCTURE_LEVEL, VISITATION_COVERAGE);

  private final Map<String, String> lsmSurveyDefaultDisplayColumns = Map.of(
      ALL_OTHER_LEVELS,
      SURVEY_COVERAGE,DIRECTLY_ABOVE_STRUCTURE_LEVEL, SURVEY_COVERAGE);

  private final Map<String, String> mdaLiteDefaultDisplayColumns = Map.of(
      ALL_OTHER_LEVELS,

      TOTAL_LIVING_ON_THE_STREET,DIRECTLY_ABOVE_STRUCTURE_LEVEL, TOTAL_LIVING_ON_THE_STREET);

  private final Map<String, String> mdaLiteDefaultDisplayColumnsOld = Map.ofEntries(
      entry(ALB + DIRECTLY_ABOVE_STRUCTURE_LEVEL, MALES_1_4),
      entry(MZB + DIRECTLY_ABOVE_STRUCTURE_LEVEL, MALES_1_4),
      entry(PZQ + DIRECTLY_ABOVE_STRUCTURE_LEVEL, MALES_1_4), entry(ALB + CDD_LEVEL, MALES_1_4),
      entry(MZB + CDD_LEVEL, MALES_1_4), entry(PZQ + CDD_LEVEL, MALES_1_4),
      entry(ALB + SUPERVISOR_LEVEL, ADVERSE), entry(MZB + SUPERVISOR_LEVEL, ADVERSE),
      entry(PZQ + SUPERVISOR_LEVEL, ADVERSE), entry(ALB + IS_ON_PLAN_TARGET, MALES_1_4),
      entry(MZB + IS_ON_PLAN_TARGET, MALES_1_4), entry(PZQ + IS_ON_PLAN_TARGET, MALES_1_4),
      entry(ALB + STRUCTURE_LEVEL, MALES_1_4), entry(MZB + STRUCTURE_LEVEL, MALES_1_4),
      entry(PZQ + STRUCTURE_LEVEL, MALES_1_4),
      entry(ALB + ALL_OTHER_LEVELS, STH_TREATMENT_COVERAGE),
      entry(MZB + ALL_OTHER_LEVELS, STH_TREATMENT_COVERAGE),
      entry(PZQ + ALL_OTHER_LEVELS, SCH_TREATMENT_COVERAGE));

  private final Map<String, String> mdaLiteDefaultDisplayColumnsWithType =
      Map.ofEntries(
          entry(MdaLiteReportType.POPULATION_DISTRIBUTION.name() , PERCENTAGE_VISITED_HEALTH_FACILITY_AFTER_SNAKE_BITE),

          entry(MdaLiteReportType.DRUG_DISTRIBUTION.name().concat(STH), ADVERSE_REACTION.concat("(").concat(STH).concat(")")),
          entry(MdaLiteReportType.DRUG_DISTRIBUTION.name().concat(SCH), ADVERSE_REACTION.concat("(").concat(SCH).concat(")")),

          entry(MdaLiteReportType.TREATMENT_COVERAGE.name() , STH_TOTAL_TREATED));


  private Long operationalAreaVisitedThreshold = 20L;
  private Long operationalAreaVisitedEffectivelyThreshold = 85L;

  private final Map<String, String> mdaLiteFilters = Map.of(NTD, STH);

  private final Map<ReportTypeEnum, Map<String, String>> dashboardFilterAssociations = Map.of(
      ReportTypeEnum.MDA_LITE_COVERAGE, mdaLiteFilters);

  private final Map<PlanInterventionTypeEnum, List<String>> detailedPerformanceLevelColumns = Map.of(
      PlanInterventionTypeEnum.IRS,
      List.of(IrsPerformanceDashboardService.DAY,
          IrsPerformanceDashboardService.FOUND,
          IrsPerformanceDashboardService.SPRAYED,
          IrsPerformanceDashboardService.NOT_SPRAYED_REFUSED,
          IrsPerformanceDashboardService.NOT_SPRAYED_REFUSED_OTHER,
          IrsPerformanceDashboardService.NOT_SPRAYED,
          IrsPerformanceDashboardService.BOTTLES_USAGE_RATE,
          IrsPerformanceDashboardService.BOTTLES_USED,
          PerformanceDashboardService.START_TIME,
          PerformanceDashboardService.END_TIME,
          PerformanceDashboardService.HOURS_WORKED,
          IrsPerformanceDashboardService.CHECKED,
          IrsPerformanceDashboardService.SPRAYED_DIFF,
          IrsPerformanceDashboardService.FOUND_DIFF),
      PlanInterventionTypeEnum.IRS_LITE,
      List.of(IrsLitePerformanceDashboardService.FOUND, IrsLitePerformanceDashboardService.SPRAYED,
          IrsLitePerformanceDashboardService.NOT_SPRAYED, PerformanceDashboardService.START_TIME,
          PerformanceDashboardService.END_TIME, PerformanceDashboardService.HOURS_WORKED));


  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ColumnMeta {

    private String name;
    private Boolean isPercentage;
  }
}
