package com.revealprecision.revealstreams.persistence.repository;


import com.revealprecision.revealstreams.persistence.domain.Location;
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


  @Query(value = "select lr.parentLocation from LocationRelationship lr "
      + "where lr.location.identifier = :locationIdentifier "
      + "and lr.locationHierarchy.identifier = :hierarchyIdentifier")
  Location getParentLocationByLocationIdAndHierarchyId(
      @Param("locationIdentifier") UUID locationIdentifier,
      @Param("hierarchyIdentifier") UUID hierarchyIdentifier);


  LocationRelationship getLocationRelationshipByLocation_IdentifierAndLocationHierarchy_Identifier(
      UUID locationIdentifier, UUID hierarchyIdentifier);
}
