package com.revealprecision.revealstreams.util;


import static com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus.PARTIALLY_SPRAYED;
import static com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus.TASKS_INCOMPLETE;

import com.revealprecision.revealstreams.constants.FormConstants.BusinessStatus;
import com.revealprecision.revealstreams.constants.FormConstants.Colors;
import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.domain.Report;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DashboardUtils {
  public static String getGeoNameDirectlyAboveStructure(Plan plan) {
    String geoNameDirectlyAboveStructure = null;
    if (plan.getLocationHierarchy().getNodeOrder().contains(LocationConstants.STRUCTURE)) {
      geoNameDirectlyAboveStructure = plan.getLocationHierarchy().getNodeOrder()
          .get(plan.getLocationHierarchy().getNodeOrder().indexOf(LocationConstants.STRUCTURE) - 1);
    }
    return geoNameDirectlyAboveStructure;
  }

  public static ColumnData getStringValueColumnData() {
    return ColumnData.builder().dataType("string").build();
  }
  public static ColumnData getIntValueColumnData() {
    return ColumnData.builder().dataType("integer").build();
  }

  public static ColumnData getLocationBusinessState(Report report) {
    ColumnData column = getStringValueColumnData();
    if (report != null && report.getReportIndicators().getBusinessStatus() != null) {
      column.setValue(report.getReportIndicators().getBusinessStatus());
    } else {
      column.setValue(BusinessStatus.NOT_VISITED);
    }
    return column;
  }

  public static String getBusinessStatusColor(String businessStatus) {
    switch (businessStatus) {
      case BusinessStatus.NOT_VISITED:
        return Colors.yellow;
      case BusinessStatus.INELIGIBLE:
      case BusinessStatus.FAMILY_NO_TASK_REGISTERED:
        return Colors.grey;
      case BusinessStatus.NOT_SPRAYED:
      case BusinessStatus.NOT_DISPENSED:
      case BusinessStatus.IN_PROGRESS:
      case BusinessStatus.NONE_RECEIVED:
      case BusinessStatus.MDA_REFUSED_OR_ABSENT:
      case BusinessStatus.REFUSED_OR_PERMANENTLY_ABSENT:
        return Colors.red;
      case BusinessStatus.SPRAYED:
      case BusinessStatus.SMC_COMPLETE:
      case BusinessStatus.SPAQ_COMPLETE:
      case BusinessStatus.ALL_TASKS_COMPLETE:
      case BusinessStatus.COMPLETE:
      case BusinessStatus.FULLY_RECEIVED:
      case BusinessStatus.MDA_COMPLETE:
      case PARTIALLY_SPRAYED:
        return Colors.green;
      case BusinessStatus.NOT_SPRAYABLE:
      case BusinessStatus.NOT_ELIGIBLE:
        return Colors.black;
      case BusinessStatus.INCOMPLETE:
      case TASKS_INCOMPLETE:
      case BusinessStatus.MDA_PARTIALLY_COMPLETE:
      case BusinessStatus.PARTIALLY_RECEIVED:
      case BusinessStatus.PARTIALLY_COMPLETE_OR_TEMPORARILY_ABSENT:
        return Colors.orange;
      default:
        log.debug(String.format("business status ( %s ) is not defined", businessStatus));
        return null;
    }
  }
  public static Double getDouble(Object object){

    if (object instanceof Integer){
      return ((Integer)object).doubleValue();
    } else if (object instanceof String){
      return Double.valueOf((String) object);
    } else if (object instanceof Double){
      return (double) object;
    }

    return null;
  }
}
