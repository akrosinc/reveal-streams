package com.revealprecision.revealstreams.models.amdr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Xaxis implements Serializable {

  private String type;
  @JsonProperty("tickvals")
  private List<String> tickVals;
  @JsonProperty("ticktext")
  private List<String> tickText;
  @JsonProperty("fixedrange")
  private boolean fixedRange;
  @JsonProperty("tickfont")
  private Font tickFont;

}
