package com.revealprecision.revealstreams.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@ToString
public class FieldAggregate extends Message {

  private Long count;
  private Long Sum;
  private Long latest;
}
