package com.revealprecision.revealstreams.models.amdr;


import com.revealprecision.revealstreams.persistence.projection.amdr.AmdrTotalsLandingPageProjection;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmdrTotalsLandingPageData implements Serializable {

  private Integer passiveCases;
  private Integer rcdCases;
  private Integer cases;
  private Integer parasitologyReports;
  private Integer importedSequences;

  public AmdrTotalsLandingPageData(AmdrTotalsLandingPageProjection landingPageProjection){
    this.cases = landingPageProjection.getCases();
    this.rcdCases = landingPageProjection.getRcdCases();
    this.importedSequences = landingPageProjection.getImportedSequences();
    this.parasitologyReports = landingPageProjection.getParasitologyReports();
    this.passiveCases = landingPageProjection.getPassiveCases();
  }

}

