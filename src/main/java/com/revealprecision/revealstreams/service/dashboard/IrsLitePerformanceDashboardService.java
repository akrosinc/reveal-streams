package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.constants.FormConstants.IRS_LITE_FOUND;
import static com.revealprecision.revealstreams.constants.FormConstants.IRS_LITE_NOT_SPRAYED;
import static com.revealprecision.revealstreams.constants.FormConstants.IRS_LITE_SPRAYED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_END_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_HOURS_WORKED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_START_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.DAYS_WORKED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.END_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.HOURS_WORKED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.START_TIME;

import com.revealprecision.revealstreams.constants.KafkaConstants;
import com.revealprecision.revealstreams.enums.PlanInterventionTypeEnum;
import com.revealprecision.revealstreams.messaging.message.UserLevel;
import com.revealprecision.revealstreams.messaging.message.UserPerformanceAggregate;
import com.revealprecision.revealstreams.messaging.message.UserPerformancePerDate;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.props.KafkaProperties;
import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IrsLitePerformanceDashboardService {

  private final DashboardProperties dashboardProperties;
  private final StreamsBuilderFactoryBean getKafkaStreams;
  private final KafkaProperties kafkaProperties;

  public static final String FOUND = "Found";
  public static final String SPRAYED = "Sprayed";
  public static final String NOT_SPRAYED = "Not Sprayed";


  boolean datastoresInitialized = false;

  private ReadOnlyKeyValueStore<String, UserPerformanceAggregate> userPerformanceSums;

  public List<RowData> getDetailedPerformanceColumnData(
       Plan plan, String id) {

    String key = plan.getIdentifier() + "_" + id;

    UserPerformanceAggregate userSumAggregate = userPerformanceSums.get(key);
    if (userSumAggregate != null) {

      return userSumAggregate.getDatedUserRecords().entrySet().stream().map(userPerformancePerDate -> {
        RowData rowData = new RowData();
        rowData.setUserName(userPerformancePerDate.getKey().toString());
        rowData.setUserId(userSumAggregate.getUser().getUserId());
        rowData.setUserType("date");
        rowData.setUserLabel("date");
        rowData.setUserParent(id);
        rowData.setColumnDataMap(
            Objects.requireNonNull(getDetailedPerformanceColumnData(
                    PlanInterventionTypeEnum.valueOf(
                        plan.getInterventionType().getCode()), userPerformancePerDate)).stream()
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));
        return rowData;
      }).collect(Collectors.toList());
    } else {
      return null;
    }
  }

  public List<SimpleEntry<String, ColumnData>> getDetailedPerformanceColumnData(
      PlanInterventionTypeEnum planInterventionTypeEnum,
      Entry<LocalDate, UserPerformancePerDate> userPerformancePerDate) {
    return dashboardProperties.getDetailedPerformanceLevelColumns().get(planInterventionTypeEnum)
        .stream()
        .map(column -> {
              switch (column) {

                case SPRAYED:
                  ColumnData sprayedColumnData = new ColumnData();
                  sprayedColumnData.setValue(
                      userPerformancePerDate.getValue().getFieldAggregate().get(IRS_LITE_SPRAYED) == null
                          ? 0 :
                          userPerformancePerDate.getValue().getFieldAggregate().get(IRS_LITE_SPRAYED)
                              .get(Boolean.TRUE.toString()) == null ? 0 :
                              userPerformancePerDate.getValue().getFieldAggregate().get(IRS_LITE_SPRAYED)
                                  .get(Boolean.TRUE.toString())
                                  .getCount());

                  return new SimpleEntry<>(SPRAYED, sprayedColumnData);

                case NOT_SPRAYED:
                  ColumnData notSprayedColumnData = new ColumnData();
                  notSprayedColumnData.setValue(
                      userPerformancePerDate.getValue().getFieldAggregate().get(IRS_LITE_NOT_SPRAYED)
                          == null
                          ? 0
                          : userPerformancePerDate.getValue().getFieldAggregate().get(
                                  IRS_LITE_NOT_SPRAYED)
                              .get(Boolean.TRUE.toString()) == null ? 0 :
                              userPerformancePerDate.getValue().getFieldAggregate()
                                  .get(IRS_LITE_NOT_SPRAYED)
                                  .get(Boolean.TRUE.toString())
                                  .getCount());
                  return new SimpleEntry<>(NOT_SPRAYED, notSprayedColumnData);

                case FOUND:
                  ColumnData foundColumnData = new ColumnData();
                  foundColumnData.setValue(
                      userPerformancePerDate.getValue().getFieldAggregate().get(IRS_LITE_FOUND) == null ? 0
                          :
                              userPerformancePerDate.getValue().getFieldAggregate().get(IRS_LITE_FOUND)
                                  .get(Boolean.TRUE.toString()) == null ? 0 :
                                  userPerformancePerDate.getValue().getFieldAggregate().get(
                                          IRS_LITE_FOUND)
                                      .get(Boolean.TRUE.toString()).getCount());
                  return new SimpleEntry<>(FOUND, foundColumnData);

                case HOURS_WORKED:
                  ColumnData hoursWorkedColumnData = new ColumnData();
                  hoursWorkedColumnData.setValue(userPerformancePerDate.getValue().getMinutesWorked());
                  return new SimpleEntry<>(HOURS_WORKED, hoursWorkedColumnData);

                case START_TIME:
                  ColumnData startTimeColumnData = new ColumnData();
                  startTimeColumnData.setValue(userPerformancePerDate.getValue().getMinStartTime());
                  return new SimpleEntry<>(START_TIME, startTimeColumnData);

                case END_TIME:
                  ColumnData endTimeColumnData = new ColumnData();
                  endTimeColumnData.setValue(userPerformancePerDate.getValue().getMaxEndTime());
                  return new SimpleEntry<>(END_TIME, endTimeColumnData);
              }
              return null;
            }
        ).collect(Collectors.toList());
  }

  private ColumnData getAverageHoursWorkedColumnData(
      UserPerformanceAggregate userPerformanceAggregate) {

    ColumnData averageHoursWorkedColumnData = new ColumnData();
    averageHoursWorkedColumnData.setDataType("string");
    averageHoursWorkedColumnData.setValue(userPerformanceAggregate.getAverageHoursWorked());
    return averageHoursWorkedColumnData;
  }

  private ColumnData getAverageStartTimeColumnData(
      UserPerformanceAggregate userPerformanceAggregate) {

    ColumnData averageHoursWorkedColumnData = new ColumnData();
    averageHoursWorkedColumnData.setDataType("string");
    averageHoursWorkedColumnData.setValue(userPerformanceAggregate.getAverageStartTime());
    return averageHoursWorkedColumnData;
  }

  private ColumnData getAverageEndTimeColumnData(
      UserPerformanceAggregate userPerformanceAggregate) {

    ColumnData averageHoursWorkedColumnData = new ColumnData();
    averageHoursWorkedColumnData.setDataType("string");
    averageHoursWorkedColumnData.setValue(userPerformanceAggregate.getAverageEndTime());
    return averageHoursWorkedColumnData;
  }

  private ColumnData getDaysWorkedColumnData(UserPerformanceAggregate userPerformanceAggregate) {

    ColumnData averageHoursWorkedColumnData = new ColumnData();
    averageHoursWorkedColumnData.setDataType("string");
    averageHoursWorkedColumnData.setValue(userPerformanceAggregate.getDaysWorked());
    return averageHoursWorkedColumnData;
  }

  private ColumnData getFoundStructure(UserPerformanceAggregate userPerformanceAggregate) {

    ColumnData averageHoursWorkedColumnData = new ColumnData();

    averageHoursWorkedColumnData.setValue(
        userPerformanceAggregate.getCountOfFieldForValue(IRS_LITE_FOUND, Boolean.TRUE));
    return averageHoursWorkedColumnData;
  }

  private ColumnData getSprayedStructure(UserPerformanceAggregate userPerformanceAggregate) {

    ColumnData averageHoursWorkedColumnData = new ColumnData();

    averageHoursWorkedColumnData.setValue(
        userPerformanceAggregate.getCountOfFieldForValue(IRS_LITE_SPRAYED, Boolean.TRUE));
    return averageHoursWorkedColumnData;
  }

  private ColumnData getNotSprayedStructure(UserPerformanceAggregate userPerformanceAggregate) {

    ColumnData averageHoursWorkedColumnData = new ColumnData();

    averageHoursWorkedColumnData.setValue(
        userPerformanceAggregate.getCountOfFieldForValue(IRS_LITE_NOT_SPRAYED, Boolean.TRUE));
    return averageHoursWorkedColumnData;
  }


  public RowData getRowData(Plan plan, UserLevel userLevel, String parentUserLevelId) {

    RowData rowData = new RowData();

    Map<String, ColumnData> columnData = new HashMap<>();

    String key = plan.getIdentifier() + "_" + userLevel.getUserId();

    UserPerformanceAggregate userSumAggregate = userPerformanceSums.get(key);

    if (userSumAggregate == null){
      return null;
    }

    ColumnData averageHoursWorkedColumnData = getAverageHoursWorkedColumnData(
        userSumAggregate);
    columnData.put(AVERAGE_HOURS_WORKED, averageHoursWorkedColumnData);

    ColumnData averageStartTimeColumnData = getAverageStartTimeColumnData(
        userSumAggregate);
    columnData.put(AVERAGE_START_TIME, averageStartTimeColumnData);

    ColumnData averageEndTimeColumnData = getAverageEndTimeColumnData(
        userSumAggregate);
    columnData.put(AVERAGE_END_TIME, averageEndTimeColumnData);

    ColumnData daysWorkedColumnData = getDaysWorkedColumnData(
        userSumAggregate);
    columnData.put(DAYS_WORKED, daysWorkedColumnData);

    ColumnData foundStructure = getFoundStructure(
        userSumAggregate);
    columnData.put(FOUND, foundStructure);

    ColumnData sprayedStructure = getSprayedStructure(
        userSumAggregate);
    columnData.put(SPRAYED, sprayedStructure);

    ColumnData notSprayed = getNotSprayedStructure(
        userSumAggregate);
    columnData.put(NOT_SPRAYED, notSprayed);


    rowData.setColumnDataMap(columnData);
    rowData.setUserName(userLevel.getName());
    rowData.setUserId(userLevel.getUserId());
    rowData.setUserLabel(userLevel.getLabel());
    rowData.setUserType(userLevel.getType());
    rowData.setUserParent(parentUserLevelId);
    return rowData;
  }

  public void initialDataStores() {
    if (!datastoresInitialized) {

      this.userPerformanceSums = getKafkaStreams.getKafkaStreams().store(
          StoreQueryParameters.fromNameAndType(kafkaProperties.getStoreMap().get(
                  KafkaConstants.userPerformanceSums),
              QueryableStoreTypes.keyValueStore()));

      datastoresInitialized = true;
    }
  }

}
