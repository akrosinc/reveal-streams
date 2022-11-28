package com.revealprecision.revealstreams.persistence.projection;

public interface LocationMetadataDoubleAggregateProjection {

  String getLocationIdentifier();
  String getLocationName();

  String getLocationParentIdentifier();
  String getLocationParentName();
  int getValue();

}
