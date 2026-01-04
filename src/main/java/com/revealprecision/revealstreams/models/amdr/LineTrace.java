package com.revealprecision.revealstreams.models.amdr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineTrace extends Trace {

  private Line line;
  private String mode;
}
