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
      "SELECT t.parentLocationIdentifier as parentLocationIdentifier, t.planIdentifier as planIdentifier, count(t) as locationCount from TaskBusinessStateTracker t "
          + "where t.parentLocationIdentifier = :parentLocationIdentifier"
          + " and t.taskLocationGeographicLevelName = :taskLocationGeographicLevelName and "
          + "t.planIdentifier = :planIdentifier and t.locationHierarchyIdentifier = :locationHierarchyIdentifier and t.taskBusinessStatus = :taskBusinessStatus"
          + " group by t.parentLocationIdentifier, t.planIdentifier ")
  LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
      UUID planIdentifier, UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      String taskBusinessStatus, UUID locationHierarchyIdentifier);



  @Query(
      value =
          "SELECT CAST(t.parent_location_identifier as varchar) as parentLocationIdentifier, CAST(t.plan_identifier as varchar) as planIdentifier, count(t) as locationCount "
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
      "SELECT DISTINCT new TaskBusinessStateTracker (t.taskLocationIdentifier,t.taskLocationGeographicLevelName,t.taskLocationName,t.taskBusinessStatus) from TaskBusinessStateTracker t WHERE t.planIdentifier = :planIdentifier "
          + "and t.locationHierarchyIdentifier = :locationHierarchyIdentifier and t.taskLocationIdentifier = :taskLocationIdentifier")
  TaskBusinessStateTracker findDistinctTaskBusinessStateTrackerByLocationHierarchyIdentifierAndTaskLocationIdentifierAndPlanIdentifier(
      UUID locationHierarchyIdentifier, UUID taskLocationIdentifier, UUID planIdentifier);

  @Query(
      value =
          "SELECT CAST(tbst.parent_location_identifier as varchar) as parentLocationIdentifier"
              + ", CAST(tbst.plan_identifier  as varchar) as planIdentifier"
              + ", tbst.task_business_status as taskBusinessStatus"
              + ", count(*) as locationCount from task_business_state_tracker tbst\n"
              + "WHERE tbst.parent_location_identifier = :parentLocationIdentifier and tbst.task_location_geographic_level_name = :taskLocationGeographicLevelName \n"
              + "and tbst.plan_identifier = :planIdentifier and tbst.location_hierarchy_identifier = :locationHierarchyIdentifier \n"
              + "group by tbst.parent_location_identifier, tbst.plan_identifier, tbst.task_business_status", nativeQuery = true)
  Set<LocationBusinessStateCount> getLocationBusinessStateObjPerGeoLevel(UUID planIdentifier,
      UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      UUID locationHierarchyIdentifier);

  @Query(value = "SELECT  Cast(t.plan_identifier as varchar) as planIdentifier\n"
      + "     ,Cast(t.parentLocationIdentifier as varchar) as parentLocationIdentifier \n"
      + "     ,t.parentName\n"
      + "     ,t.parentgeographicLevel\n"
      + "     ,t.business_status as taskBusinessStatus, count(*) as locationCount from (\n"
      + "                  SELECT l.identifier  as locationIdentifier,\n"
      + "                         l.name as locationName,\n"
      + "                         pl.identifier as parentLocationIdentifier,\n"
      + "                         pl.name as parentName,\n"
      + "                         pl.location_property->>'geographicLevel' as parentgeographicLevel,\n"
      + "                         t.business_status,\n"
      + "                         t.plan_identifier\n"
      + "                  from location l\n"
      + "                           left join (\n"
      + "                      select *\n"
      + "                      from location_relationship lr,\n"
      + "                           lateral unnest(lr.ancestry) with ordinality parent(parent, post)\n"
      + "                  ) lr on lr.location_identifier = l.identifier\n"
      + "                           left join location pl on pl.identifier = lr.parent\n"
      + "                           inner join task t on l.identifier = t.base_entity_identifier\n"
      + "              ) t\n"
      + "WHERE t.parentLocationIdentifier = :parentLocationIdentifier and t.plan_identifier = :planIdentifier\n"
      + "group by t.parentLocationIdentifier, t.plan_identifier, t.parentName,t.parentgeographicLevel, t.business_status",nativeQuery = true)
  Set<LocationBusinessStateCount> getLocationBusinessStateCount(UUID planIdentifier,
      UUID parentLocationIdentifier);


  @Query(
      value =
          "SELECT CAST(ebsaet.parent as varchar) as parentLocationIdentifier, CAST(ebsaet.plan_identifier as varchar) as planIdentifier,\n"
              + "       ebsaet.businessStatus as taskBusinessStatus, count(*) as locationCount\n"
              + "from event_business_state_and_event_tracker ebsaet\n"
              + "left join plan p on p.identifier = ebsaet.plan_identifier\n"
              + "WHERE ebsaet.parent = :parentLocationIdentifier \n"
              + "  and ebsaet.plan_identifier = :planIdentifier \n"
              + "    and p.hierarchy_identifier = :locationHierarchyIdentifier \n"
              + "group by ebsaet.parent,ebsaet.businessStatus,ebsaet.plan_identifier", nativeQuery = true)
  Set<LocationBusinessStateCount> getLocationBusinessStateObjPerGeoLevelFromEventTracker(UUID planIdentifier,
      UUID parentLocationIdentifier,
      UUID locationHierarchyIdentifier);

  @Query(value = "SELECT count(*) from task_business_state_tracker tbst\n"
      + "where tbst.parent_location_identifier = :parentLocationIdentifier and tbst.plan_identifier = :planIdentifier", nativeQuery = true)
  long getTotalLocationsByParentAndPlan(UUID planIdentifier,
      UUID parentLocationIdentifier);

}
