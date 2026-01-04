package com.revealprecision.revealstreams.dto.amdr;

import com.revealprecision.revealstreams.models.amdr.AmdrChartData;
import com.revealprecision.revealstreams.models.amdr.AmdrTotalsLandingPageData;
import com.revealprecision.revealstreams.models.amdr.AmdrTotalsPercentageLandingPageData;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmdrLandPageResponse implements Serializable {

  private AmdrChartData parasitologyData;
  private AmdrChartData importData;
  private AmdrChartData parasitologyImportData;
  private AmdrTotalsLandingPageData amdrTotalsLandingPageData;
  private AmdrTotalsPercentageLandingPageData amdrTotalsPercentageLandingPageData;
}
