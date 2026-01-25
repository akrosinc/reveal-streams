package com.revealprecision.revealstreams.persistence.domain.amdr;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HslColor implements Serializable {

  private int h;
  private int s;
  private int l;
}
