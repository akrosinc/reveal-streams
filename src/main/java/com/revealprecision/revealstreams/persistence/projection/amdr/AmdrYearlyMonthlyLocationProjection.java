package com.revealprecision.revealstreams.persistence.projection.amdr;

public interface AmdrYearlyMonthlyLocationProjection {

  String getLocationIdentifier();
  String getCollectionMonth();
  String getCollectionYear();
  String getData();
}
