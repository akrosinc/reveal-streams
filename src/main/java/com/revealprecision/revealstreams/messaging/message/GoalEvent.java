package com.revealprecision.revealstreams.messaging.message;


import com.revealprecision.revealstreams.persistence.domain.AbstractAuditableEntity;
import com.revealprecision.revealstreams.enums.PriorityEnum;
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
public class GoalEvent extends AbstractAuditableEntity {

  private UUID identifier;

  private String description;

  private PriorityEnum priority;

  private PlanEvent plan;

}
