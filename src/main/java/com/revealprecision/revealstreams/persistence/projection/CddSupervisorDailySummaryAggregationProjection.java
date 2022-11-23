package com.revealprecision.revealstreams.persistence.projection;

public interface CddSupervisorDailySummaryAggregationProjection {

  String getLocationIdentifier();

  String getLocationName();

  int getTotalTreated();

  int getTotalLivingOnTheStreet();

  int getTotalBittenBySnake();

  int getTotalVisitedHealthFacilityAfterSnakeBite();

  int getTotalPeopleLivingWithDisability();

  int getAdministered();

  int getDamaged();

  int getAdverse();

  int getTotalTreatedMaleOneToFour();

  int getTotalTreatedMaleFiveToFourteen();

  int getTotalTreatedMaleAboveFifteen();

  int getTotalTreatedFemaleOneToFour();

  int getTotalTreatedFemaleFiveToFourteen();

  int getTotalTreatedFemaleAboveFifteen();
}
