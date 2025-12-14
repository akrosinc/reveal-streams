package com.revealprecision.revealstreams.persistence.projection.amdr;

import java.util.UUID;

public interface AmdrDataProjection {

  UUID getId();

  UUID getLocationId();

  String getType();

  Double getOverallValue();
}
