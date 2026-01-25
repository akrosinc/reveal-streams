package com.revealprecision.revealstreams.persistence.domain.amdr;

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

@Entity
@Table(schema = "amdr")
@Setter @Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmdrHeaderNames {

  @Id
  @GeneratedValue
  private String key;

  private String name;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private HslColor color;

}
