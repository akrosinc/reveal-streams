package com.revealprecision.revealstreams.persistence.repository;


import com.revealprecision.revealstreams.persistence.domain.TaskBusinessStateTracker;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskBusinessStateTrackerRepository extends
    JpaRepository<TaskBusinessStateTracker, UUID> {

  @Query(
      "SELECT DISTINCT new TaskBusinessStateTracker (t.taskLocationIdentifier,t.parentGeographicLevelName,t.taskLocationName,t.taskBusinessStatus) from TaskBusinessStateTracker t WHERE t.planIdentifier = :planIdentifier "
          + "and t.locationHierarchyIdentifier = :locationHierarchyIdentifier and t.taskLocationIdentifier = :taskLocationIdentifier")
  TaskBusinessStateTracker findTaskBusinessStateTrackerByLocationHierarchyIdentifierAndTaskLocationIdentifierAndPlanIdentifier(
      UUID locationHierarchyIdentifier, UUID taskLocationIdentifier, UUID planIdentifier);

  @Query(
      "SELECT t.parentLocationIdentifier as parentLocationIdentifier, t.planIdentifier as planIdentifier, count(t) as locationCount from TaskBusinessStateTracker t "
          + "where t.parentLocationIdentifier = :parentLocationIdentifier"
          + " and t.taskLocationGeographicLevelName = :taskLocationGeographicLevelName and "
          + "t.planIdentifier = :planIdentifier and t.locationHierarchyIdentifier = :locationHierarchyIdentifier and t.taskBusinessStatus = :taskBusinessStatus"
          + " group by t.parentLocationIdentifier, t.planIdentifier ")
  LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
      UUID planIdentifier, UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      String taskBusinessStatus, UUID locationHierarchyIdentifier);

  @Query(
      "SELECT t.parentLocationIdentifier as parentLocationIdentifier, t.planIdentifier as planIdentifier,t.taskBusinessStatus as taskBusinessStatus, count(t) as locationCount from TaskBusinessStateTracker t "
          + "where t.parentLocationIdentifier = :parentLocationIdentifier"
          + " and t.taskLocationGeographicLevelName = :taskLocationGeographicLevelName and "
          + "t.planIdentifier = :planIdentifier and t.locationHierarchyIdentifier = :locationHierarchyIdentifier"
          + " group by t.parentLocationIdentifier, t.planIdentifier,t.taskBusinessStatus ")
  Set<LocationBusinessStateCount> getLocationBusinessStateObjPerGeoLevel(UUID planIdentifier,
      UUID parentLocationIdentifier, String taskLocationGeographicLevelName);

  @Query(
      "SELECT DISTINCT new TaskBusinessStateTracker (t.taskLocationIdentifier,t.taskLocationGeographicLevelName,t.taskLocationName,t.taskBusinessStatus) from TaskBusinessStateTracker t WHERE t.planIdentifier = :planIdentifier "
          + "and t.locationHierarchyIdentifier = :locationHierarchyIdentifier and t.taskLocationIdentifier = :taskLocationIdentifier")
  TaskBusinessStateTracker findDistinctTaskBusinessStateTrackerByLocationHierarchyIdentifierAndTaskLocationIdentifierAndPlanIdentifier(
      UUID locationHierarchyIdentifier, UUID taskLocationIdentifier, UUID planIdentifier);

  @Query(
      "SELECT t.parentLocationIdentifier as parentLocationIdentifier, t.planIdentifier as planIdentifier,t.taskBusinessStatus as taskBusinessStatus, count(t) as locationCount from TaskBusinessStateTracker t "
          + "where t.parentLocationIdentifier = :parentLocationIdentifier"
          + " and t.taskLocationGeographicLevelName = :taskLocationGeographicLevelName and "
          + "t.planIdentifier = :planIdentifier and t.locationHierarchyIdentifier = :locationHierarchyIdentifier"
          + " group by t.parentLocationIdentifier, t.planIdentifier,t.taskBusinessStatus ")
  Set<LocationBusinessStateCount> getLocationBusinessStateObjPerGeoLevel(UUID planIdentifier,
      UUID parentLocationIdentifier, String taskLocationGeographicLevelName,UUID locationHierarchyIdentifier);


}
