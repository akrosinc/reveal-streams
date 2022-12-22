package com.revealprecision.revealstreams.persistence.projection;


public interface OnchocerciasisSurveyCddSummaryAggregationProjection {

  String getLocationIdentifier();

  String getLocationName();

  String getTreatmentLocationType();

  int getTotalTreatedMaleFiveFourteen();

  int getTotalTreatedMaleAboveFifteen();

  int getTotalTreatedFemaleFiveFourteen();

  int getTotalTreatedFemaleAboveFifteen();

  int getTotalTreated();

  int getTotalUntreatedAbsent();

  int getTotalUntreatedRefusal();

  int getTotalUntreatedPregnant();

  int getTotalUntreatedSick();

  int getTotalUntreatedUnderFive();

  int getTotalUntreated();

  String getHouseholdHead();

  String getHouseholdHeadPhoneNumber();

  int getNumberOfStructures();


}
