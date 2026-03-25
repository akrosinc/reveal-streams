package com.revealprecision.revealstreams.persistence.domain.amdr;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(schema = "amdr")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Setter @Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmdrDataCalMat {

  @EmbeddedId
  private AmdrDataCalcMatId id;

  private Long totalRecs;
  private Long totalWild;
  private Long totalMono;
  private Long totalMixed;
}
