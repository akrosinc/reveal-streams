package com.revealprecision.revealstreams.messaging.message;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LocationFormDataCountAggregateEvent extends Message {

  private Long count = 0L;
  private UUID entityTagIdentifier;
  private Object countKey;

}
