package com.revealprecision.revealstreams.persistence.projection;

public interface TabletAccountabilityAggregationProjection {
  String getLocationIdentifier();
  String getLocationName();
  int getPzqReturned();
  int getMbzReturned();
  int getMzbNumberHouseholdsVisited();
  int getPzqNumberHouseholdsVisited();
}
