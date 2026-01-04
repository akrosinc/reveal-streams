package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Line implements Serializable {

  private String color;
  private int width;
}
