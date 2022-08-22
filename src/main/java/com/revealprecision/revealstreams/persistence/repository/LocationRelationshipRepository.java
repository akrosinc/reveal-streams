package com.revealprecision.revealstreams.persistence.repository;


import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.projection.LocationChildrenCountProjection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.revealprecision.revealstreams.persistence.domain.LocationRelationship;


@Repository
public interface LocationRelationshipRepository extends JpaRepository<LocationRelationship, UUID> {


  @Query(value = "SELECT count(*) FROM location_relationship lr "
      + "                  INNER JOIN location l on l.identifier = lr.location_identifier "
      + "                  LEFT JOIN geographic_level gl on l.geographic_level_identifier = gl.identifier "
      + "where lr.location_hierarchy_identifier = :locationHierarchyIdentifier "
      + "  AND gl.name = :geographicLevelName AND "
      + "        CAST(STRING_TO_ARRAY(:locationIdentifier,',')as uuid[]) && lr.ancestry", nativeQuery = true)
  Long getNumberOfChildrenByGeoLevelNameWithinLocationAndHierarchy(
      @Param("geographicLevelName") String geographicLevelName,
      @Param("locationIdentifier") String locationIdentifier,
      @Param("locationHierarchyIdentifier") UUID locationHierarchyIdentifier);

  @Query(value = "select lr.location "
      + "from LocationRelationship lr "
      + "where lr.locationHierarchy.identifier = :hierarchyIdentifier "
      + "and lr.parentLocation.identifier = :locationIdentifier")
  List<Location> getChildren(@Param("hierarchyIdentifier") UUID hierarchyIdentifier,
      @Param("locationIdentifier") UUID locationIdentifier);

  @Query(value = "select "
      + "new com.revealprecision.revealstreams.dto.PlanLocationDetails(lr.location, count(pl), count(pa)) from LocationRelationship lr "
      + "inner join PlanLocations pl on lr.location.identifier = pl.location.identifier and pl.plan.identifier = :planIdentifier "
      + "left join PlanAssignment pa on pa.planLocations.identifier = pl.identifier "
      + "where lr.parentLocation.identifier = :parentLocationIdentifier group by lr.identifier")
  List<PlanLocationDetails> getAssignedLocationDetailsByParentIdAndPlanId(
      @Param("parentLocationIdentifier") UUID parentLocationIdentifier,
      @Param("planIdentifier") UUID planIdentifier);

  @Query(value =
      "select cast(lr.parent_identifier as varchar) as parentIdentifier, count(lr.parent_identifier) as childrenCount from location_relationship lr "
          + "where lr.location_hierarchy_identifier = :locationHierarchyIdentifier "
          + "group by lr.parent_identifier", nativeQuery = true)
  List<LocationChildrenCountProjection> getLocationChildrenCount(UUID locationHierarchyIdentifier);

  @Query(value = "select "
      + "new com.revealprecision.revealstreams.dto.PlanLocationDetails(lr.location, count(pl), count(pa)) from LocationRelationship lr "
      + "left join PlanLocations pl on lr.location.identifier = pl.location.identifier and pl.plan.identifier = :planIdentifier "
      + "left join PlanAssignment pa on pa.planLocations.identifier = pl.identifier "
      + "where lr.parentLocation.identifier is null group by lr.identifier")
  PlanLocationDetails getRootLocationDetailsByAndPlanId(
      @Param("planIdentifier") UUID planIdentifier);

  @Query(value = "select lr.parentLocation from LocationRelationship lr "
      + "where lr.location.identifier = :locationIdentifier "
      + "and lr.locationHierarchy.identifier = :hierarchyIdentifier")
  Location getParentLocationByLocationIdAndHierarchyId(
      @Param("locationIdentifier") UUID locationIdentifier,
      @Param("hierarchyIdentifier") UUID hierarchyIdentifier);

  @Query(value = "select "
      + "new com.revealprecision.revealstreams.dto.PlanLocationDetails(lr.location, count(pl), count(pa)) from LocationRelationship lr "
      + "left join PlanLocations pl on lr.location.identifier = pl.location.identifier and pl.plan.identifier = :planIdentifier "
      + "left join PlanAssignment pa on pa.planLocations.identifier = pl.identifier "
      + "where lr.parentLocation.identifier = :parentLocationIdentifier group by lr.identifier")
  List<PlanLocationDetails> getLocationDetailsByParentIdAndPlanId(
      @Param("parentLocationIdentifier") UUID parentLocationIdentifier,
      @Param("planIdentifier") UUID planIdentifier);


  LocationRelationship getLocationRelationshipByLocation_IdentifierAndLocationHierarchy_Identifier(
      UUID locationIdentifier, UUID hierarchyIdentifier);

  @Query(value =
      "select cast(lr.parent_identifier as varchar) as parentIdentifier, count(lr.parent_identifier) as childrenCount from location_relationship lr "
          + "inner join plan_locations pl on pl.plan_identifier = :planIdentifier and pl.location_identifier = lr.location_identifier "
          + "where lr.location_hierarchy_identifier = :locationHierarchyIdentifier "
          + "group by lr.parent_identifier", nativeQuery = true)
  List<LocationChildrenCountProjection> getLocationAssignedChildrenCount(
      UUID locationHierarchyIdentifier, UUID planIdentifier);
}
