package com.revealprecision.revealstreams.models.amdr;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarTrace extends Trace {
  private List<Integer> base = new ArrayList<>();
}
