package com.revealprecision.revealstreams.api;


import com.revealprecision.revealstreams.dto.AmdrFeatureSetResponse;
import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.dto.amdr.AmdrLandPageResponse;
import com.revealprecision.revealstreams.enums.ApplicableReportsEnum;
import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.enums.ReportTypeEnum;
import com.revealprecision.revealstreams.models.AdditionalReportInfo;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.service.PlanService;
import com.revealprecision.revealstreams.service.dashboard.AmdrService;
import com.revealprecision.revealstreams.service.dashboard.DashboardService;
import com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/dashboard")

public class ReportDashboardController {

  private final DashboardService dashboardService;
  private final PlanService planService;
  private final DashboardProperties dashboardProperties;
  private final PerformanceDashboardService performanceDashboardService;
  private final AmdrService amdrService;

  @GetMapping("/reportTypes")
  public ReportTypeEnum[] getReportTypes() {
    return ReportTypeEnum.values();
  }

  @GetMapping("/applicableReports/{identifier}")
  public List<String> getReportTypes(@PathVariable("identifier") UUID identifier) {
    Plan plan = planService.findPlanByIdentifier(identifier);
    return ApplicableReportsEnum.valueOf(plan.getInterventionType().getCode()).getReportName();
  }


  @GetMapping("/reportData")
  public ResponseEntity<FeatureSetResponse> getDataForReports(
      @RequestParam(name = "reportType") String reportType,
      @RequestParam(name = "planIdentifier",required = false) UUID planIdentifier,
      @RequestParam(name = "parentIdentifier", required = false) String parentIdentifier,
      @RequestParam(name = "filters", required = false) List<String> filters,
      @RequestParam(name = "type", required = false, defaultValue = "TREATMENT_COVERAGE") MdaLiteReportType type,
      @RequestParam(name = "clickedColumn", required = false) String clickedColumn) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(dashboardService.getDataForReport(reportType, planIdentifier, parentIdentifier,
            filters, type,clickedColumn));
  }

  @GetMapping("/amdr/reportData")
  public ResponseEntity<AmdrFeatureSetResponse> getAmdrDataForReports(
      @RequestParam(name = "parentIdentifier", required = false) String parentIdentifier,
      @RequestParam(name = "clickedColumn", required = false) String clickedColumn) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(amdrService.getDataForReport(parentIdentifier
            ,clickedColumn));
  }

  @GetMapping("/amdr/landingPageData")
  public AmdrLandPageResponse getAmdrLandingPageDataForReports() {
    return amdrService.getLandingPageData3();
  }

  @GetMapping("/reportAdditionalInfo")
  public ResponseEntity<AdditionalReportInfo> getDataForReports(
      @RequestParam(name = "reportType") String reportType) {
    Map<String, String> dashboardFilter = null;

    try {
      ReportTypeEnum type = ReportTypeEnum.valueOf(reportType);
      dashboardFilter =
          dashboardProperties.getDashboardFilterAssociations().get(type);
    } catch (IllegalArgumentException | NullPointerException ignored) {
      // no match → leave dashboardFilter as null
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(AdditionalReportInfo.builder()
            .dashboardFilter(dashboardFilter)
            .reportTypeEnum(reportType)
            .columnClickable(
                dashboardProperties.getColumnClickableReports().getOrDefault(reportType, false))
            .showMap(dashboardProperties.getShowMap().getOrDefault(reportType,false))
            .showGraphs(dashboardProperties.getShowGraph().getOrDefault(reportType,false))
            .show3dGraphs(dashboardProperties.getShow3dGraph().getOrDefault(reportType,false))
            .build()
        );
  }

  @GetMapping("/performance-data")
  public ResponseEntity<List<RowData>> getDataForPerformance(
      @RequestParam(name = "planIdentifier") UUID planIdentifier,
      @RequestParam(name = "key", required = false) String key) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(performanceDashboardService.getDataForReport(planIdentifier, key));
  }

  @GetMapping("/detailed-performance-data")
  public ResponseEntity<List<RowData>> getDetailedDataForPerformance(
      @RequestParam(name = "planIdentifier") UUID planIdentifier,
      @RequestParam(name = "key") String key) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(performanceDashboardService.getDatedRowDatas(planIdentifier, key));
  }
}
