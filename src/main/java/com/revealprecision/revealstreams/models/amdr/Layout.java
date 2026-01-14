package com.revealprecision.revealstreams.models.amdr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Layout implements Serializable {

  private Xaxis xaxis;
  private Yaxis yaxis;

  @JsonProperty("barmode")
  private String barMode;

  @JsonProperty("bargap")
  private int barGap;

  private Title title;

  private Legend legend;
}
