package com.revealprecision.revealstreams.enums;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LookupEntityTypeCodeEnum implements Serializable {
  PERSON_CODE("Person"), LOCATION_CODE("Location"), GROUP_CODE("Group");

  private final String lookupEntityType;

}
