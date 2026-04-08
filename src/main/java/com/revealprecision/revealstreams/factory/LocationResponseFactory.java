package com.revealprecision.revealstreams.factory;


import com.revealprecision.revealstreams.dto.LocationPropertyResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.persistence.domain.Location;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationResponseFactory {

  public static LocationResponse fromPlanLocationDetails(PlanLocationDetails planLocationDetails,
      UUID parentIdentifier) {
    return LocationResponse.builder()
        .identifier(planLocationDetails.getLocation().getIdentifier())
        .type(planLocationDetails.getLocation().getType())
        .geometry(planLocationDetails.getLocation().getGeometry())
        .properties(
            LocationPropertyResponse.builder()
                .name(planLocationDetails.getLocation().getName())
                .status(planLocationDetails.getLocation().getStatus())
                .externalId(planLocationDetails.getLocation().getExternalId())
                .geographicLevel(planLocationDetails.getLocation().getGeographicLevel().getName())
                .numberOfTeams(planLocationDetails.getAssignedTeams())
                .assigned(planLocationDetails.getAssignedLocations() != null
                    && planLocationDetails.getAssignedLocations() > 0)
                .parentIdentifier(planLocationDetails.getParentLocation() == null ? parentIdentifier
                    : planLocationDetails.getParentLocation().getIdentifier())
                .childrenNumber(planLocationDetails.getChildrenNumber()!= null ? planLocationDetails.getChildrenNumber() : 0L)
                .build())
        .build();
  }

  public static LocationResponse fromPlanLocationDetailsForDate(Location locationDetails) {
    return LocationResponse.builder()
        .identifier(locationDetails.getIdentifier())
        .type(locationDetails.getType())
        .geometry(locationDetails.getGeometry())
        .properties(
            LocationPropertyResponse.builder()
                .name(locationDetails.getName())
                .status(locationDetails.getStatus())
                .externalId(locationDetails.getExternalId())
                .geographicLevel(locationDetails.getGeographicLevel().getName())
                .build())
        .build();
  }


}
