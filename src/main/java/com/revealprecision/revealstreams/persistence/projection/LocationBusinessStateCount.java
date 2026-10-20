package com.revealprecision.revealstreams.persistence.projection;

public interface LocationBusinessStateCount {

  String getParentLocationIdentifier();

  String getPlanIdentifier();

  String getTaskBusinessStatus();

  String getActionTitle();

  String getActionIdentifier();

  Long getLocationCount();
}
