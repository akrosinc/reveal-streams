package com.revealprecision.revealstreams.persistence.projection;

public interface LocationRelationshipProjection {

  String getIdentifier();
  String getLocationName();
  String getLocationIdentifier();
  String getParentIdentifier();
  String getGeographicLevelName();
}
