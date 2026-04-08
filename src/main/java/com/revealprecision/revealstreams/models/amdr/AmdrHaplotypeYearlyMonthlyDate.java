package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AmdrHaplotypeYearlyMonthlyDate implements Serializable {

  private String name;
  private Map<String,Map<String, AmdrDrugMarkerStats>> data;
}
