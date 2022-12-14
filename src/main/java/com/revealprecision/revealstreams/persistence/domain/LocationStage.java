package com.revealprecision.revealstreams.persistence.domain;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

@FieldNameConstants
@Entity
@Audited
@Getter
@Setter
@SQLDelete(sql = "UPDATE location SET entity_status = 'DELETED' where identifier=?")
@Where(clause = "entity_status='ACTIVE'")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class LocationStage extends AbstractAuditableEntity {

  @Id
  @GeneratedValue
  private UUID identifier;

  @ColumnDefault(value = "feature")
  private String type;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private Geometry geometry;

  private String name;
  private String status;
  private UUID externalId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "geographic_level_identifier")
  private GeographicLevel geographicLevel;

  @ManyToOne
  @JoinColumn(name = "location_bulk_identifier")
  private LocationBulk locationBulk;

  @ManyToMany(mappedBy = "locations")
  private Set<Person> people;


}
