package com.revealprecision.revealstreams.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureSetResponse {

  private UUID identifier;
  private String type;
  private String defaultDisplayColumn;
  private List<LocationResponse> features;
  private List<LocationResponse> parents;
}
