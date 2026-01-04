package com.revealprecision.revealstreams.models.amdr;


import com.revealprecision.revealstreams.persistence.projection.amdr.LandingPageProjection;
import java.io.Serializable;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmdrLandingPageData implements Serializable {

  private Double year;
  private String month;
  private Integer passiveCases;
  private Integer rcdCases;
  private Integer cases;
  private Integer parasitologyReports;
  private Integer balanceParasitology;
  private Integer rollingBalanceParasitology;
  private Integer importedSequences;
  private Integer balanceImport;
  private Integer rollingBalanceImport;
  private YearMonth yearMonth;

  // Constructor that accepts the projection interface
  public AmdrLandingPageData(LandingPageProjection projection) {
    this.year = projection.getYear();
    this.month = projection.getMonth();
    this.passiveCases = projection.getPassiveCases();
    this.rcdCases = projection.getRcdCases();
    this.cases = projection.getCases();
    this.parasitologyReports = projection.getParasitologyReports();
    this.balanceParasitology = projection.getBalanceParasitology();
    this.rollingBalanceParasitology = projection.getRollingBalanceParasitology();
    this.importedSequences = projection.getImportedSequences();
    this.balanceImport = projection.getBalanceImport();
    this.rollingBalanceImport = projection.getRollingBalanceImport();
    this.yearMonth = YearMonth.of(projection.getYear().intValue(),Integer.parseInt(projection.getMonth()));
  }

}

