package com.revealprecision.revealstreams.util;


import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.persistence.domain.Plan;

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
}
