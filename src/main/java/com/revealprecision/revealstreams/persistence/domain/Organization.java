package com.revealprecision.revealstreams.persistence.domain;

import com.revealprecision.revealstreams.enums.OrganizationTypeEnum;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
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
@Audited
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE organization SET entity_status = 'DELETED' where identifier=?")
@Where(clause = "entity_status='ACTIVE'")
public class Organization extends AbstractAuditableEntity {

  @Id
  @GeneratedValue
  private UUID identifier;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private boolean active;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrganizationTypeEnum type;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private Set<Organization> children;

  @ManyToOne
  @JoinColumn(name = "organization_parent_id", nullable = true)
  private Organization parent;

  @ManyToMany(mappedBy = "organizations")
  private Set<User> users;

  @OneToMany(mappedBy = "organization")
  private Set<PlanAssignment> planAssignments;


  @PreRemove
  private void preRemove() {
    children.forEach(organization -> organization.setParent(null));
  }
}
