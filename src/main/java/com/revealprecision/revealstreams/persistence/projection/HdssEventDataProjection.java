package com.revealprecision.revealstreams.persistence.projection;

public interface HdssEventDataProjection {

  String getCompound();
  int getPassiveTested();
  int getRcdTested();
  int getPassivePositive();
  int getRcdPositive();
  int getTotalTested();
  int getTotalCases();
}
