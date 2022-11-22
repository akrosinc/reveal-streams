package com.revealprecision.revealstreams.persistence.projection;

public interface CddDrugReceivedAggregationProjection {
  String getLocationIdentifier();
  String getLocationName();
  int getPzqReceived();
  int getMbzReceived();
}
