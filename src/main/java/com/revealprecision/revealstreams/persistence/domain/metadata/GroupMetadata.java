package com.revealprecision.revealstreams.persistence.domain.metadata;

import com.revealprecision.revealstreams.persistence.domain.AbstractAuditableEntity;
import com.revealprecision.revealstreams.persistence.domain.Group;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.MetadataList;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Setter @Getter
@Audited
@SQLDelete(sql = "UPDATE form SET entity_status = 'DELETED' where identifier=?")
@Where(clause = "entity_status='ACTIVE'")
public class GroupMetadata extends AbstractAuditableEntity {

  @Id
  @GeneratedValue
  private UUID identifier;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private MetadataList entityValue;

  @OneToOne
  @JoinColumn(name = "group_identifier")
  private Group group;

}
