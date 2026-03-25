package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import lombok.Data;

@Data
public class AmdrMarkerStats implements Serializable {
  private Long wild;
  private Long mono;
  private Long mixed;
  private Long totalRecs;
}
