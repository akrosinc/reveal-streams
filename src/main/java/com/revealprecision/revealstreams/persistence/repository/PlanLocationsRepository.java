package com.revealprecision.revealstreams.persistence.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.revealprecision.revealstreams.persistence.domain.PlanLocations;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanLocationsRepository extends EntityGraphJpaRepository<PlanLocations, UUID> {


  @Query(value = "SELECT count(*) "
      + "FROM plan_locations pl "
      + "         INNER JOIN location_relationship lr on pl.location_identifier = lr.location_identifier "
      + "         INNER JOIN location l on l.identifier = lr.location_identifier "
      + "         LEFT JOIN geographic_level gl on l.geographic_level_identifier = gl.identifier "
      + "where lr.location_hierarchy_identifier = :locationHierarchyIdentifier  "
      + "  AND gl.name = :geographicLevelName  "
      + "  AND CAST(STRING_TO_ARRAY(:locationIdentifier, ',') as uuid[]) && lr.ancestry "
      + "  and pl.plan_identifier = :planIdentifier ", nativeQuery = true)
  Long getNumberOfAssignedChildrenByGeoLevelNameWithinLocationAndHierarchyAndPlan(
      @Param("geographicLevelName") String geographicLevelName,
      @Param("locationIdentifier") String locationIdentifier,
      @Param("locationHierarchyIdentifier") UUID locationHierarchyIdentifier,
      @Param("planIdentifier") UUID planIdentifier);

  @Query(value = "SELECT ls.location_count from plan_locations pl "
      + "left join location_counts ls on pl.location_identifier = ls.parent_location_identifier "
      + "where pl.plan_identifier = :planIdentifier and pl.location_identifier = :locationIdentifier and "
      + "ls.geographic_level_name = :geographicLevelName", nativeQuery = true)
  Long getAssignedLocationCountOfGeoLevelByLocationParentAndPlan(UUID planIdentifier, UUID locationIdentifier, String geographicLevelName);

}