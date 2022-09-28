package com.revealprecision.revealstreams.messaging.message;

import java.time.LocalTime;
import java.util.Map;
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
public class UserPerformancePerDate extends Message {

  private Long minutesWorked;
  private LocalTime minStartTime;
  private LocalTime maxEndTime;
  private Long startTimeLong;
  private Long endTimeLong;
  private Map<String, Object> fields;
  private Map<String, Map<String, FieldAggregate>> fieldAggregate;
}
