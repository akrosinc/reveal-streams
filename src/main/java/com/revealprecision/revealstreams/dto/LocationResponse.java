package com.revealprecision.revealstreams.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.revealprecision.revealstreams.persistence.domain.Geometry;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class LocationResponse {

  private UUID identifier;
  private String type;
  private Geometry geometry;
  private Boolean isActive;
  private LocationPropertyResponse properties;
}
