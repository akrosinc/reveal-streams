package com.revealprecision.revealstreams.persistence.projection;

import java.time.LocalTime;


public interface UserPerformanceDistrictAggregateProjection {

  String getDate();
  String getDistrict();
  String getDeviceUser();
  String getFieldWorker();
  int getFound();
  int getSprayed();
  int getNotSprayed();
  int getNotSprayedOther();
  int getNotSprayedRefused();
  int getSachetCount();
  int getDaysWorked();
  LocalTime getStartTime();
  LocalTime getEndTime();
  String getAverageStartTime();
  String getAverageEndTime();
  String getAverageDuration();
  String getDurationWorked();
  int getSprayDiff();
  int getFoundDiff();
  String getSprayChecked();
  String getFoundChecked();
  int getSpraySummary();
  int getFoundSummary();
  boolean getChecked();

}
