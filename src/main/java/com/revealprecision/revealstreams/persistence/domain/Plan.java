package com.revealprecision.revealstreams.persistence.domain;


import com.revealprecision.revealstreams.enums.PlanStatusEnum;
import com.revealprecision.revealstreams.persistence.generator.PlanServerVersionGenerator;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GeneratorType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@SQLDelete(sql = "UPDATE plan SET entity_status = 'DELETED' where identifier=?")
@Where(clause = "entity_status='ACTIVE'")
public class Plan extends AbstractAuditableEntity {

  @Id
  @GeneratedValue
  private UUID identifier;
  private String name;
  private String title;
  private LocalDate date;
  private LocalDate effectivePeriodStart;
  private LocalDate effectivePeriodEnd;

  @GeneratorType(type = PlanServerVersionGenerator.class, when = GenerationTime.ALWAYS)
  private Long serverVersion;

  @ManyToOne
  @JoinColumn(name = "hierarchy_identifier")
  private LocationHierarchy locationHierarchy;

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<PlanLocations> planLocations;

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
  private Set<Goal> goals;

  @NotNull
  @Enumerated(EnumType.STRING)
  private PlanStatusEnum status;

  @OneToOne(mappedBy = "plan", fetch = FetchType.EAGER,  cascade = CascadeType.ALL)
  private PlanTargetType planTargetType;

  @ManyToOne
  @JoinColumn(name = "lookup_intervention_type_identifier")
  private LookupInterventionType interventionType;

}