package com.revealprecision.revealstreams.dto;


import com.revealprecision.revealstreams.persistence.domain.Geometry;
import com.revealprecision.revealstreams.persistence.domain.LocationProperty;
import javax.validation.constraints.NotNull;
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
public class LocationRequest {

  private String type;

  @NotNull
  private Geometry geometry;

  @NotNull
  private LocationProperty properties;
}
