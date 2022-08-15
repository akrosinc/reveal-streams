package com.revealprecision.revealstreams.messaging.message;


import com.revealprecision.revealstreams.enums.ProcessTrackerEnum;
import com.revealprecision.revealstreams.enums.ProcessType;
import com.revealprecision.revealstreams.persistence.domain.TaskProcessStage;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProcessTrackerEvent extends Message{

  private UUID identifier;

  private UUID planIdentifier;

  private ProcessType processType;

  private UUID processTriggerIdentifier;

  private ProcessTrackerEnum state;

  private List<TaskProcessStage> taskGenerationStages;

}
