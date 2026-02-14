package com.revealprecision.revealstreams.persistence.projection.amdr;

public interface AmdrPerformanceDataProjection {

  String getParentIdentifier();

  String getParentName();

  String getLocationIdentifier();

  String getLocationName();

  String getVerified();

  long getCount();
}
