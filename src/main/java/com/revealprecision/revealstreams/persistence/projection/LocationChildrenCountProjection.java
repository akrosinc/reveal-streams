package com.revealprecision.revealstreams.persistence.projection;

public interface LocationChildrenCountProjection {

  String getParentIdentifier();
  Long getChildrenCount();
}
