package com.revealprecision.revealstreams.persistence.projection;

import java.util.UUID;

public interface LocationFlat {

  UUID getLocationIdentifier();

  UUID getParentIdentifier();

  String getName();


  UUID getGeoLevelIdentifier();
  String getGeoLevelName();
}
