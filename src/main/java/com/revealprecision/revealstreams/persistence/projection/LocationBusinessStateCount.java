package com.revealprecision.revealstreams.persistence.projection;

public interface LocationBusinessStateCount {

  String getParentLocationIdentifier();

  String getPlanIdentifier();

  String getBusinessStatus();

  Long getLocationCount();
}
