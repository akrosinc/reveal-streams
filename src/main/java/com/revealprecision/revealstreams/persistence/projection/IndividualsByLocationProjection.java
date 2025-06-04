package com.revealprecision.revealstreams.persistence.projection;

public interface IndividualsByLocationProjection {

  String getLocationIdentifier();

  String getLocationName();

  int getIndividualCount();
}
