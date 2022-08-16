package com.revealprecision.revealstreams.persistence.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.revealprecision.revealstreams.persistence.domain.PlanLocations;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanLocationsRepository extends EntityGraphJpaRepository<PlanLocations, UUID> {


  @Query(value = "SELECT count(*)\n"
      + "FROM plan_locations pl\n"
      + "         INNER JOIN location_relationship lr on pl.location_identifier = lr.location_identifier\n"
      + "         INNER JOIN location l on l.identifier = lr.location_identifier\n"
      + "         LEFT JOIN geographic_level gl on l.geographic_level_identifier = gl.identifier\n"
      + "where lr.location_hierarchy_identifier = :locationHierarchyIdentifier \n"
      + "  AND gl.name = :geographicLevelName \n"
      + "  AND CAST(STRING_TO_ARRAY(:locationIdentifier, ',') as uuid[]) && lr.ancestry\n"
      + "  and pl.plan_identifier = :planIdentifier ", nativeQuery = true)
  Long getNumberOfAssignedChildrenByGeoLevelNameWithinLocationAndHierarchyAndPlan(
      @Param("geographicLevelName") String geographicLevelName,
      @Param("locationIdentifier") String locationIdentifier,
      @Param("locationHierarchyIdentifier") UUID locationHierarchyIdentifier,
      @Param("planIdentifier") UUID planIdentifier);
}