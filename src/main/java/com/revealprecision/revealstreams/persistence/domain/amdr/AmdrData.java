package com.revealprecision.revealstreams.persistence.domain.amdr;

import com.revealprecision.revealstreams.models.amdr.AmdrCounters;
import com.revealprecision.revealstreams.models.amdr.AmdrOverall;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(schema = "amdr")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Setter @Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmdrData {

  @Id
  @GeneratedValue
  private UUID id;

  private UUID locationId;

  private String type;

  private Double overallValue;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private Map<String,Map<String, AmdrCounters>> data;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private AmdrOverall overallObject;

  private LocalDateTime datetime;

  private String collectionYear;

  private String collectionMonth;

}
