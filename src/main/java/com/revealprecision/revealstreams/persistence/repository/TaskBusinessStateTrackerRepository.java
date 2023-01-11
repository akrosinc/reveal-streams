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
      value = "SELECT CAST(t.parent_location_identifier as varchar) as parentLocationIdentifier, CAST(t.plan_identifier as varchar) as planIdentifier, count(t) as locationCount "
          + "from task_business_state_tracker t "
          + "left join location l on l.identifier = t.task_location_identifier "
          + "where t.parent_location_identifier = :parentLocationIdentifier "
          + "and t.task_location_geographic_level_name = :taskLocationGeographicLevelName and  "
          + "t.plan_identifier = :planIdentifier  and t.location_hierarchy_identifier = :locationHierarchyIdentifier "
          + "and l.location_property ->>'surveyLocationType'='waterbody' "
          + "and t.task_business_status = :taskBusinessStatus "
          + "group by t.parent_location_identifier, t.plan_identifier ", nativeQuery = true)
  LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForWaterBodies(
      UUID planIdentifier, UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      String taskBusinessStatus, UUID locationHierarchyIdentifier);

  @Query(
      value = "SELECT CAST(t.parent_location_identifier as varchar) as parentLocationIdentifier"
          + ", CAST(t.plan_identifier  as varchar) as planIdentifier, count(t) as locationCount "
            + "from task_business_state_tracker t "
            + "left join location l on l.identifier = t.task_location_identifier "
            + "where t.parent_location_identifier = :parentLocationIdentifier "
            + "and t.task_location_geographic_level_name = :taskLocationGeographicLevelName and  "
            + "t.plan_identifier = :planIdentifier  and t.location_hierarchy_identifier = :locationHierarchyIdentifier "
            + "and jsonb_exists_any(l.location_property,ARRAY['surveyLocationType'])=false "
            + "and t.task_business_status = :taskBusinessStatus "
            + "group by t.parent_location_identifier, t.plan_identifier ", nativeQuery = true)
  LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForNonWaterBodies(
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

  @Query(value = "SELECT count(*) from task_business_state_tracker tbst\n"
      + "where tbst.parent_location_identifier = :parentLocationIdentifier and tbst.plan_identifier = :planIdentifier;",nativeQuery = true)
  long getTotalLocationsByParentAndPlan(UUID planIdentifier,
      UUID parentLocationIdentifier);

}
