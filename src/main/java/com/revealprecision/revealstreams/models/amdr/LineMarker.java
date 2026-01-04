package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineMarker extends Marker implements Serializable {
  private int size;
}
