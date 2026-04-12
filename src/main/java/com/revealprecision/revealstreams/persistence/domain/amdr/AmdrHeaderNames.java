package com.revealprecision.revealstreams.persistence.domain.amdr;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(schema = "amdr")
@Setter @Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmdrHeaderNames {

  @EmbeddedId
  private AmdrHeaderNamesId id;

  private String name;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private HslColor color;

  @Column(insertable = false,updatable = false)
  private String colType;

  private String colParent;

  @Type(type = "list-array")
  private List<String> contributingCol;

  private int colOrder;
}
