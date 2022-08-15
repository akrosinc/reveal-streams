package com.revealprecision.revealstreams.props;

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

  private List<String> dashboards = List.of("IRS_COVERAGE", "MDA_COVERAGE");
  private Long operationalAreaVisitedThreshold = 20L;
  private Long operationalAreaVisitedEffectivelyThreshold = 85L;

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ColumnMeta {

    private String name;
    private Boolean isPercentage;
  }
}
