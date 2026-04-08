package com.revealprecision.revealstreams.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.revealprecision.revealstreams.models.amdr.AmdrDrugYearlyMonthlyLocational;
import com.revealprecision.revealstreams.models.amdr.AmdrMarkerStats;
import com.revealprecision.revealstreams.service.dashboard.AmdrService.CoordsByYearOrLocationWithTicks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmdrFeatureSetResponse {
  private UUID identifier;
  private String type;
  private String defaultDisplayColumn;
  private List<LocationResponse> features;
  private List<LocationResponse> parents;
  private Boolean noLocationData;
  private Boolean noDashboardData;
  private CoordsByYearOrLocationWithTicks coords;
  private List<LocationPropertyResponse> rows = new ArrayList<>();;
  private List<AmdrDrugYearlyMonthlyLocational> amdrDrugYearlyMonthlyLocationals = new ArrayList<>();
  private  Map<String, Map<String, AmdrMarkerStats>> markers;
}
