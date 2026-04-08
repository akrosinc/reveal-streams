package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import lombok.Data;

@Data
public class AmdrDrugMarkerStats implements Serializable {
  private Long wild;
  private Long mono;
  private Long mixed;
  private Long val;
  private Long rec;
}
