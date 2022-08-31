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

  @Query(value = "SELECT sum(asc_.structure_count) from assigned_structure_counts asc_ "
      + "where asc_.plan_identifier = :planIdentifier and asc_.parent_location_identifier = :parentLocationIdentifier "
      + "group by asc_.parent_location_identifier, asc_.parent_location_name", nativeQuery = true)
  Long getAssignedStructureCountByLocationParentAndPlan(UUID planIdentifier,
      UUID parentLocationIdentifier);


  @Query(value = "SELECT DISTINCT structure_count from assigned_structure_counts asc_ "
      + "WHERE asc_.plan_identifier = :planIdentifier and asc_.location_identifier = :parentLocationIdentifier", nativeQuery = true)
  Long getAssignedStructureCountOnPlanTargetByLocationParentAndPlan(UUID planIdentifier,
      UUID parentLocationIdentifier);

}