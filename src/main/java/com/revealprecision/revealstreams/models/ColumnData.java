package com.revealprecision.revealstreams.models;

import com.revealprecision.revealstreams.persistence.domain.amdr.HslColor;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString
public class ColumnData implements Serializable {

  private Object value;
  private Boolean isPercentage;
  private Boolean isHidden;
  private String meta;
  private String dataType = "double";
  private String description;
  private HslColor hslColor;
}
