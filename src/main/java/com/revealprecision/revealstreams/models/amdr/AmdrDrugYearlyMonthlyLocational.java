package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AmdrDrugYearlyMonthlyLocational implements Serializable {

  private String locationIdentifier;
  private List<AmdrDrugYearlyMonthlyLocationalItem> items;
}
