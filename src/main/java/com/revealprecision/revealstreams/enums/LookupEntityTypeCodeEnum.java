package com.revealprecision.revealstreams.enums;

import com.revealprecision.revealstreams.exceptions.WrongEnumException;
import com.revealprecision.revealstreams.exceptions.constant.Error;
import java.io.Serializable;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LookupEntityTypeCodeEnum implements Serializable {
  PERSON_CODE("Person"), LOCATION_CODE("Location"), GROUP_CODE("Group");

  private final String lookupEntityType;
  public static LookupEntityTypeCodeEnum lookup(String lookupEntityType) {
    return Stream.of(LookupEntityTypeCodeEnum.values()).filter(
            lookupEntityTypeCodeEnum -> lookupEntityTypeCodeEnum.getLookupEntityType()
                .equals(lookupEntityType)).findFirst()
        .orElseThrow(() -> new WrongEnumException(String.format(
            Error.WRONG_ENUM, LookupEntityTypeCodeEnum.class.getSimpleName(), lookupEntityType)));
  }
}
