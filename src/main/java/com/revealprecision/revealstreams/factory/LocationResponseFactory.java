package com.revealprecision.revealstreams.factory;


import com.revealprecision.revealstreams.dto.LocationPropertyResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.SummaryEnum;
import com.revealprecision.revealstreams.persistence.domain.Location;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elasticsearch.search.SearchHit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
                .assigned(planLocationDetails.getAssignedLocations() > 0)
                .parentIdentifier(planLocationDetails.getParentLocation() == null ? parentIdentifier
                    : planLocationDetails.getParentLocation().getIdentifier())
                .childrenNumber(planLocationDetails.getChildrenNumber())
                .build())
        .build();
  }




}
