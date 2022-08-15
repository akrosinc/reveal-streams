package com.revealprecision.revealstreams.messaging.message;


import com.revealprecision.revealstreams.enums.ProcessTrackerEnum;
import com.revealprecision.revealstreams.enums.TaskProcessEnum;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskProcessEvent extends Message {


  private UUID identifier;

  private UUID baseEntityIdentifier;

  private ProcessTrackerEvent processTracker;

  private ProcessTrackerEnum state;

  private String owner;

  private ActionEvent actionEvent;

  private PlanEvent planEvent;

  private TaskProcessEnum taskProcessEnum;

  private UUID taskIdentifier;
}
