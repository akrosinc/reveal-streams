package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
public class AmdrHaplotypeYearlyMonthlyLocationalItem implements Serializable {

  private Long collectionYear;
  private Long collectionMonth;

  private Map<String,Map<String, AmdrDrugMarkerStats>> data;
}
