package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.LocationAboveStructure;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationAboveStructureCountsRepository extends JpaRepository<LocationAboveStructure, UUID> {


  @Query("SELECT lasc from LocationAboveStructure lasc WHERE lasc.parentLocationIdentifier = :parentLocationIdentifier and "
      + "lasc.locationHierarchyIdentifier = :locationHierarchyIdentifier and lasc.locationAboveStructureIdentifier = :locationAboveStructureIdentifier"
      + " and lasc.planIdentifier = :planIdentifier")
  LocationAboveStructure getLocationAboveStructureByCompositePrimaryKey(UUID parentLocationIdentifier, UUID locationHierarchyIdentifier, UUID locationAboveStructureIdentifier
  , UUID planIdentifier);


  @Query("SELECT lasc from LocationAboveStructure lasc WHERE "
      + "lasc.locationHierarchyIdentifier = :locationHierarchyIdentifier and lasc.locationAboveStructureIdentifier = :locationAboveStructureIdentifier"
      + " and lasc.planIdentifier = :planIdentifier")
  Set<LocationAboveStructure> getLocationAboveStructureByPlanLocationHierarchyAndLocationAbove(UUID locationHierarchyIdentifier, UUID locationAboveStructureIdentifier
      , UUID planIdentifier);

  @Query("SELECT count(lasc) from LocationAboveStructure lasc WHERE "
      + "      lasc.locationHierarchyIdentifier = :locationHierarchyIdentifier and lasc.parentLocationIdentifier = :locationAboveStructureIdentifier "
      + "      and lasc.planIdentifier = :planIdentifier and lasc.isVisited = true  and lasc.locationAboveStructureGeographicLevelName = :childGeographicLevelName ")
  Long getCountOfVisitedLocations(UUID locationHierarchyIdentifier, UUID locationAboveStructureIdentifier
      , UUID planIdentifier, String childGeographicLevelName);

  @Query("SELECT count(lasc) from LocationAboveStructure lasc WHERE "
      + "      lasc.locationHierarchyIdentifier = :locationHierarchyIdentifier and lasc.parentLocationIdentifier = :locationAboveStructureIdentifier "
      + "      and lasc.planIdentifier = :planIdentifier and lasc.isVisitedEffectively = true  and lasc.locationAboveStructureGeographicLevelName = :childGeographicLevelName ")
  Long getCountOfVisitedEffectivelyLocations(UUID locationHierarchyIdentifier, UUID locationAboveStructureIdentifier
      , UUID planIdentifier, String childGeographicLevelName);

  @Query("SELECT count(lasc) from LocationAboveStructure lasc WHERE "
      + "      lasc.locationHierarchyIdentifier = :locationHierarchyIdentifier and lasc.parentLocationIdentifier = :locationAboveStructureIdentifier "
      + "      and lasc.planIdentifier = :planIdentifier and lasc.isTreated = true  and lasc.locationAboveStructureGeographicLevelName = :childGeographicLevelName ")
  Long getCountOfTreatedLocations(UUID locationHierarchyIdentifier, UUID locationAboveStructureIdentifier
      , UUID planIdentifier, String childGeographicLevelName);

}
