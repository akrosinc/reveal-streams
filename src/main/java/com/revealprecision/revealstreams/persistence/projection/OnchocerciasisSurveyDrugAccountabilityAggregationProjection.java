package com.revealprecision.revealstreams.persistence.projection;


public interface OnchocerciasisSurveyDrugAccountabilityAggregationProjection {

  String getLocationIdentifier();

  String getLocationName();
  int getTabletsUsed();
  int getTabletsReceived();
  int getTabletsReturned();
  int getTabletsLost();
}
