package com.revealprecision.revealstreams.constants;

public interface KafkaConstants {

  String LOCATION_METADATA_UPDATE = "LOCATION_METADATA_UPDATE";
  String PERSON_METADATA_UPDATE = "PERSON_METADATA_UPDATE";
  String FORM_EVENT_CONSUMPTION = "FORM_EVENT_CONSUMPTION";
  String LOCATION_SUPERVISOR_CDD = "LOCATION_SUPERVISOR_CDD";
  String METADATA_AGGREGATE = "METADATA_AGGREGATE";
  String USER_PERFORMANCE_DATA = "USER_PERFORMANCE_DATA";
  String USER_PARENT_CHILD = "USER_PARENT_CHILD";
  String METADATA_COUNT_AGGREGATE = "METADATA_COUNT_AGGREGATE";
  String METADATA_MINMAX_AGGREGATE = "METADATA_MINMAX_AGGREGATE";


  String personBusinessStatus = "personBusinessStatus";
  String structurePeople = "structurePeople";
  String structurePeopleCounts = "structurePeopleCounts";
  String locationStructureBusinessStatus = "locationStructureBusinessStatus";
  String locationStructureHierarchyBusinessStatus = "locationStructureHierarchyBusinessStatus";
  String locationFormDataInteger = "locationFormDataInteger";
  String locationFormDataIntegerSumOrAverage = "locationFormDataIntegerSumOrAverage";
  String locationFormDataStringCount = "locationFormDataStringCount";
  String mdaLiteSupervisors = "mdaLiteSupervisors";
  String cddNames = "cddNames";
  String supervisorLocationFormDataIntegerSumOrAverage = "supervisorLocationFormDataIntegerSumOrAverage";
  String cddSupervisorLocationFormDataIntegerSumOrAverage = "cddSupervisorLocationFormDataIntegerSumOrAverage";
  String userPerformanceSums = "userPerformanceSums";
  String userParentChildren = "userParentChildren";
  String locationFormDataMinMax = "locationFormDataMinMax";
}
