package com.revealprecision.revealstreams.api;


import com.revealprecision.revealstreams.dto.FeatureSetResponse;
import com.revealprecision.revealstreams.enums.ApplicableReportsEnum;
import com.revealprecision.revealstreams.enums.MdaLiteReportType;
import com.revealprecision.revealstreams.enums.ReportTypeEnum;
import com.revealprecision.revealstreams.models.AdditionalReportInfo;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.service.PlanService;
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
      @RequestParam(name = "planIdentifier") UUID planIdentifier,
      @RequestParam(name = "parentIdentifier", required = false) String parentIdentifier,
      @RequestParam(name = "filters", required = false) List<String> filters,
      @RequestParam(name = "type", required = false, defaultValue = "TREATMENT_COVERAGE") MdaLiteReportType type) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(dashboardService.getDataForReport(reportType, planIdentifier, parentIdentifier,
            filters, type));
  }

  @GetMapping("/reportAdditionalInfo")
  public ResponseEntity<AdditionalReportInfo> getDataForReports(
      @RequestParam(name = "reportType") String reportType) {
    Map<String, String> dashboardFilter = dashboardProperties.getDashboardFilterAssociations()
        .get(ReportTypeEnum.valueOf(reportType));
    return ResponseEntity.status(HttpStatus.OK)
        .body(AdditionalReportInfo.builder()
            .dashboardFilter(dashboardFilter)
            .reportTypeEnum(ReportTypeEnum.valueOf(reportType))
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
