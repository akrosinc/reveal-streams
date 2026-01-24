package com.revealprecision.revealstreams.models;


import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalReportInfo implements Serializable {
  private String reportTypeEnum;
  private Map<String, String> dashboardFilter;
  private boolean columnClickable = false;
  private boolean showMap = true;
  private boolean showGraphs = false;

}
