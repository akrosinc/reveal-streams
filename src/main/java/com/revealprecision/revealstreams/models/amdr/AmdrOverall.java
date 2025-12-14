package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmdrOverall implements Serializable {
  private String type;
  private int year;
  private int overallValue;
  private int overallTotalRecs;
}
