package com.revealprecision.revealstreams.models.amdr;


import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmdrTotalsPercentageLandingPageData implements Serializable {

  private Double parasitologyToCasesPercentage;
  private Double importToCasesPercentage;
  private Double importToParasitologyPercentage;

}

