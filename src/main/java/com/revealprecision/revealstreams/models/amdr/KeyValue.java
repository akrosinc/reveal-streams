package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyValue implements Serializable {

  private String key;
  private Double number;
  private Double sub1;
  private Double sub2;
}
