package com.revealprecision.revealstreams.persistence.projection;

public interface CddDrugWithdrawalAggregationProjection {
  String getLocationIdentifier();
  String getLocationName();
  int getPzqWithdrawn();
  int getMbzWithdrawn();
}
