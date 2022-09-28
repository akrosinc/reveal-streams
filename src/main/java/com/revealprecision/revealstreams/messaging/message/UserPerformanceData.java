package com.revealprecision.revealstreams.messaging.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
public class UserPerformanceData extends Message {

  private UUID planIdentifier;
  private List<UserLevel> orgHierarchy;
  private LocalDateTime captureTime;
  private Map<String, Object> fields;
  private boolean isUndo;

}
