package com.revealprecision.revealstreams.persistence.domain.amdr;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.List;
import java.util.Map;
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
@Table(schema = "amdr", name = "amdr_mappings")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Setter @Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmdrMappings {

  @Id
  @GeneratedValue
  private int id;

  private String amdrKey;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private Map<String,List<String>> amdrSubKeys;

}
