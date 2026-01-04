package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmdrChartData implements Serializable {

  private List<Trace> data;
  private Layout layout;
}
