package com.revealprecision.revealstreams.persistence.projection;

public interface GdrsCountsProjection {

  String getRdt();
  String getParIdentifier();
  String getParName();
  String getParGeo();
  int getCount();

}
