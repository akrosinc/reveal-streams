package com.revealprecision.revealstreams.util;

import com.revealprecision.revealstreams.messaging.message.ActionEvent;
import com.revealprecision.revealstreams.enums.LookupEntityTypeCodeEnum;
import com.revealprecision.revealstreams.persistence.domain.Action;

public class ActionUtils {

  public static Boolean isActionForPerson(Action action) {
    return action.getLookupEntityType() != null && LookupEntityTypeCodeEnum.PERSON_CODE
        .getLookupEntityType().equals(
            action.getLookupEntityType().getCode());
  }

  public static Boolean isActionForLocation(Action action) {
    return action.getLookupEntityType() != null && LookupEntityTypeCodeEnum.LOCATION_CODE
        .getLookupEntityType().equals(action.getLookupEntityType().getCode());
  }
  public static Boolean isActionForPerson(ActionEvent action) {
    return action.getLookupEntityType() != null && LookupEntityTypeCodeEnum.PERSON_CODE
        .getLookupEntityType().equals(
            action.getLookupEntityType().getCode());
  }

  public static Boolean isActionForLocation(ActionEvent action) {
    return action.getLookupEntityType() != null && LookupEntityTypeCodeEnum.LOCATION_CODE
        .getLookupEntityType().equals(action.getLookupEntityType().getCode());
  }
}
