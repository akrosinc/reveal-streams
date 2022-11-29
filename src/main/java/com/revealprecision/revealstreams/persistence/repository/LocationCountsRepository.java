package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.LocationCounts;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationCountsRepository extends JpaRepository<LocationCounts, UUID> {

  LocationCounts findLocationCountsByParentLocationIdentifierAndLocationHierarchyIdentifierAndGeographicLevelName(
      UUID parentLocationIdentifier, UUID locationHierarchyIdentifier, String geographicLevelName);


  @Query(value = "SELECT CAST(lr.location_parent_identifier as varchar) as parentLocationIdentifier\n"
      + ",SUM(CAST(l.location_property->>'structures' as int)) as locationCount  From  location_relationships lr\n"
      + "inner join task t on  t.base_entity_identifier = lr.location_identifier \n"
      + "left join location l on l.identifier = lr.location_identifier\n"
      + "\n"
      + "WHERE  t.plan_identifier = :planIdentifier and t.business_status in :businessStatus and \n"
      + "lr.location_parent_identifier = :locationParentIdentifier\n"
      + "group by lr.location_parent_identifier", nativeQuery = true)
  LocationBusinessStateCount getStructureCountsByLocationParentAndBusinessStatusForLiteIntervention(
      @Param("planIdentifier") UUID planIdentifier,
      @Param("locationParentIdentifier") UUID locationParentIdentifier,
      @Param("businessStatus") List<String> businessStatus);

}
