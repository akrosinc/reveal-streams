package com.revealprecision.revealstreams.persistence.domain;

import com.revealprecision.revealstreams.enums.UnitEnum;
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

@FieldNameConstants
@Entity
@Audited
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE target SET entity_status = 'DELETED' where identifier=?")
@Where(clause = "entity_status='ACTIVE'")
public class Target extends AbstractAuditableEntity {

  @Id
  @GeneratedValue
  private UUID identifier;

  private String measure;

  private int value;

  private String comparator;

  @Enumerated(EnumType.STRING)
  private UnitEnum unit;

  private LocalDate due;

  @ManyToOne
  @JoinColumn(name = "condition_identifier")
  private Condition condition;

}
