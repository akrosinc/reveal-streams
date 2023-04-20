package com.revealprecision.revealstreams.persistence.projection;


public interface OnchocerciasisSurveyAdverseEventsAggregationProjection {

  String getLocationIdentifier();

  String getLocationName();
  int getReadminstered();
  int getAdverseEventCount();
}
