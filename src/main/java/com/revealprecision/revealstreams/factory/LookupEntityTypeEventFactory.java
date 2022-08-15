package com.revealprecision.revealstreams.factory;

import com.revealprecision.revealstreams.messaging.message.LookupEntityTypeEvent;
import com.revealprecision.revealstreams.persistence.domain.LookupEntityType;

public class LookupEntityTypeEventFactory {

  public static LookupEntityTypeEvent getLookupEntityTypeEvent(LookupEntityType lookupEntityType) {
    return LookupEntityTypeEvent.builder()
        .tableName(lookupEntityType.getTableName())
        .code(lookupEntityType.getCode())
        .identifier(lookupEntityType.getIdentifier())
        .build();
  }
}
