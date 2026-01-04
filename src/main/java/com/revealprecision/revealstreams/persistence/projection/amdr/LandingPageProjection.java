package com.revealprecision.revealstreams.persistence.projection.amdr;

public interface LandingPageProjection {

  Double getYear();

  String getMonth();

  Integer getPassiveCases();

  Integer getRcdCases();

  Integer getCases();

  Integer getParasitologyReports();

  Integer getBalanceParasitology();

  Integer getRollingBalanceParasitology();

  Integer getImportedSequences();

  Integer getBalanceImport();

  Integer getRollingBalanceImport();
}
