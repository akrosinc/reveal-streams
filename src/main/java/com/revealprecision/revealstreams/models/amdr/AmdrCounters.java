package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmdrCounters implements Serializable {
  private Double mono;
  private Double mixed;
  private Double total;
  private Double totalRecs;
}
