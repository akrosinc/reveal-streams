package com.revealprecision.revealstreams.messaging.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@AllArgsConstructor
@ToString
public class PersonBusinessStatusAggregate extends Message {

  private Map<UUID,CurrentState> personState;

  private boolean isInitialized;

  private List<UUID> locationsTreated;

  private List<UUID> personTreated;

  private List<UUID> personEligible;

  public PersonBusinessStatusAggregate(){
    this.isInitialized = false;
  }
  @Setter
  @Getter
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString
  public static class CurrentState{

    private String state;

    private LocalDateTime updateTime;

  }



}
