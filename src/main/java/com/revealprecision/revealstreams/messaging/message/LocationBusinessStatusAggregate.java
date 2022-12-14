package com.revealprecision.revealstreams.messaging.message;

import java.time.LocalDateTime;
import java.util.List;
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
public class LocationBusinessStatusAggregate extends Message {

  private UUID entityId;

  private String businessStatus;

  private LocalDateTime updateTime;

  private List<UUID> ancestry;

}
