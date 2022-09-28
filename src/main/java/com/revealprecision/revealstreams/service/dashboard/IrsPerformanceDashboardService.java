package com.revealprecision.revealstreams.service.dashboard;


import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_END_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_HOURS_WORKED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.AVERAGE_START_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.DAYS_WORKED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.END_TIME;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.HOURS_WORKED;
import static com.revealprecision.revealstreams.service.dashboard.PerformanceDashboardService.START_TIME;

import com.revealprecision.revealstreams.enums.PlanInterventionTypeEnum;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.persistence.domain.PerformanceUserType;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.projection.UserPerformanceDistrictAggregateProjection;
import com.revealprecision.revealstreams.persistence.repository.PerformanceEventTrackerRepository;
import com.revealprecision.revealstreams.persistence.repository.PerformanceUserTypeRepository;
import com.revealprecision.revealstreams.props.DashboardProperties;
import com.revealprecision.revealstreams.props.KafkaProperties;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IrsPerformanceDashboardService {

  private final DashboardProperties dashboardProperties;
  private final StreamsBuilderFactoryBean getKafkaStreams;
  private final KafkaProperties kafkaProperties;
  private final PerformanceUserTypeRepository performanceUserTypeRepository;
  private final PerformanceEventTrackerRepository performanceEventTrackerRepository;

  public static final String FOUND = "Number of Structures Found";
  public static final String DAY = "Day";
  public static final String SPRAYED = "Number of Structures Sprayed";
  public static final String AVERAGE_STRUCTURES = "Average Structures Per Day";
  public static final String DATA_QUALITY_CHECK = "Data Quality Check";
  public static final String SPRAYED_DIFF = "Sprayed Difference";
  public static final String FOUND_DIFF = "Found Difference";
  public static final String CHECKED = "Data Quality Check";
  public static final String NOT_SPRAYED = "Number of Structures Not Sprayed";
  public static final String NOT_SPRAYED_TOTAL = "Number of Structures Not Sprayed Total";
  public static final String NOT_SPRAYED_REFUSED = "Number of Structures Not Sprayed Refused";
  public static final String NOT_SPRAYED_REFUSED_OTHER = "Number of Structures Not Sprayed Other";
  public static final String BOTTLES_USED = "Insecticide Used";
  public static final String BOTTLES_USAGE_RATE = "Insecticide Usage Rate";
  public static final String NUMBER_OF_DAYS_TEAMS_IN_THE_FIELD = "Number of days Teams in the Field";

  public static final Map<String, Set<String>> columnOrder = new HashMap<>();

  static {
    Set<String> districtCols = new LinkedHashSet<>();
    districtCols.add(NUMBER_OF_DAYS_TEAMS_IN_THE_FIELD);
    districtCols.add(AVERAGE_STRUCTURES);
    districtCols.add(AVERAGE_START_TIME);
    districtCols.add(AVERAGE_END_TIME);
    districtCols.add(AVERAGE_HOURS_WORKED);
    districtCols.add(BOTTLES_USAGE_RATE);
    districtCols.add(DATA_QUALITY_CHECK);
    columnOrder.put("district", districtCols);

    Set<String> deviceUserCols = new LinkedHashSet<>();
    deviceUserCols.add(DAYS_WORKED);
    deviceUserCols.add(FOUND);
    deviceUserCols.add(SPRAYED);
    deviceUserCols.add(NOT_SPRAYED);
    deviceUserCols.add(AVERAGE_STRUCTURES);
    deviceUserCols.add(AVERAGE_START_TIME);
    deviceUserCols.add(AVERAGE_END_TIME);
    deviceUserCols.add(DATA_QUALITY_CHECK);
    columnOrder.put("deviceUser", deviceUserCols);

    Set<String> fieldWorkerCols = new LinkedHashSet<>();
    fieldWorkerCols.add(DAYS_WORKED);
    fieldWorkerCols.add(FOUND);
    fieldWorkerCols.add(AVERAGE_STRUCTURES);
    fieldWorkerCols.add(SPRAYED);
    fieldWorkerCols.add(NOT_SPRAYED);
    fieldWorkerCols.add(AVERAGE_START_TIME);
    fieldWorkerCols.add(AVERAGE_END_TIME);
    columnOrder.put("fieldWorker", fieldWorkerCols);
  }

  public List<RowData> getDetailedPerformanceColumnData(Plan plan, String id) {

    PerformanceUserType performanceUserTypeByPlanIdentifierAndUser = performanceUserTypeRepository.findPerformanceUserTypeByPlanIdentifierAndUserString(
        plan.getIdentifier(), id);
    if (performanceUserTypeByPlanIdentifierAndUser != null) {
      String name = null;
      List<UserPerformanceDistrictAggregateProjection> districtStats = null;
      switch (performanceUserTypeByPlanIdentifierAndUser.getTypeString()) {
        case "district":
          districtStats = performanceEventTrackerRepository.getDistrictStats(
              plan.getIdentifier(), id);
          name = "district";
          break;
        case "deviceUser":
          name = "deviceUser";
          districtStats = performanceEventTrackerRepository.getDeviceUserStats(plan.getIdentifier(),
              id);
          break;
        case "fieldWorker":
          name = "fieldWorker";
          districtStats = performanceEventTrackerRepository.getFieldWorkerStats(
              plan.getIdentifier(),
              id);
          break;
      }

      String finalName = name;

      List<UserPerformanceDistrictAggregateProjection> finalDistrictStats = districtStats;
      return IntStream.range(0, districtStats.size()).mapToObj(
          num -> {
            UserPerformanceDistrictAggregateProjection districtStat = finalDistrictStats.get(num);
            String colname = null;
            switch (finalName) {
              case "district":
                colname = districtStat.getDistrict();
                break;
              case "deviceUser":
                colname = districtStat.getDeviceUser();
                break;
              case "fieldWorker":
                colname = districtStat.getFieldWorker();
                break;
            }

            RowData rowData = new RowData();
            rowData.setUserName(districtStat.getDate());
            rowData.setUserId(colname);
            rowData.setUserType("date");
            rowData.setUserLabel("date");
            rowData.setUserParent(id);
            rowData.setColumnDataMap(Objects.requireNonNull(getDetailedPerformanceColumnData2(
                    PlanInterventionTypeEnum.valueOf(plan.getInterventionType().getCode()),
                    districtStat, num)
                ).stream()
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> b,
                    LinkedHashMap::new)));
            return rowData;
          }).collect(Collectors.toList());
    } else {
      return null;
    }
  }

  public List<SimpleEntry<String, ColumnData>> getDetailedPerformanceColumnData2(
      PlanInterventionTypeEnum planInterventionTypeEnum,
      UserPerformanceDistrictAggregateProjection userPerformancePerDate, int day) {
    return dashboardProperties.getDetailedPerformanceLevelColumns().get(planInterventionTypeEnum)
        .stream().map(column -> {
          switch (column) {

            case DAY:
              ColumnData dayColData = new ColumnData();
              dayColData.setValue(
                  day + 1);

              return new SimpleEntry<>(DAY, dayColData);

            case SPRAYED:
              ColumnData sprayedColumnData = new ColumnData();
              sprayedColumnData.setValue(
                  userPerformancePerDate.getSprayed());

              return new SimpleEntry<>(SPRAYED, sprayedColumnData);

            case NOT_SPRAYED:
              ColumnData notSprayedColumnData = new ColumnData();
              notSprayedColumnData.setValue(
                  userPerformancePerDate.getNotSprayed());
              return new SimpleEntry<>(NOT_SPRAYED, notSprayedColumnData);

            case NOT_SPRAYED_REFUSED:
              ColumnData notSprayedRefusedColumnData = new ColumnData();
              notSprayedRefusedColumnData.setValue(
                  userPerformancePerDate.getNotSprayedRefused());
              return new SimpleEntry<>(NOT_SPRAYED_REFUSED, notSprayedRefusedColumnData);

            case NOT_SPRAYED_REFUSED_OTHER:
              ColumnData notSprayedRefusedOtherColumnData = new ColumnData();
              notSprayedRefusedOtherColumnData.setValue(
                  userPerformancePerDate.getNotSprayedOther());
              return new SimpleEntry<>(NOT_SPRAYED_REFUSED_OTHER, notSprayedRefusedOtherColumnData);

            case FOUND:
              ColumnData foundColumnData = new ColumnData();
              foundColumnData.setValue(
                  userPerformancePerDate.getFound());
              return new SimpleEntry<>(FOUND, foundColumnData);

            case HOURS_WORKED:
              ColumnData hoursWorkedColumnData = new ColumnData();
              hoursWorkedColumnData.setValue(userPerformancePerDate.getDurationWorked());
              return new SimpleEntry<>(HOURS_WORKED, hoursWorkedColumnData);

            case BOTTLES_USED:
              ColumnData bottlesUsedColumnData = new ColumnData();
              bottlesUsedColumnData.setValue(
                  userPerformancePerDate.getSachetCount());

              return new SimpleEntry<>(BOTTLES_USED, bottlesUsedColumnData);

            case BOTTLES_USAGE_RATE:
              ColumnData bottlesUsageRateColumnData = new ColumnData();
              int sachetCount = userPerformancePerDate.getSachetCount();
              int spray = userPerformancePerDate.getSprayed();
              double usage = 0;
              if (sachetCount > 0) {
                usage =  (double) spray / (double) sachetCount ;
                bottlesUsageRateColumnData.setMeta(
                     "Sprayed Structures:" + spray + " / "  + "Insecticide: " + sachetCount );
              }
              bottlesUsageRateColumnData.setIsPercentage(true);

              bottlesUsageRateColumnData.setValue(
                  usage);

              return new SimpleEntry<>(BOTTLES_USAGE_RATE, bottlesUsageRateColumnData);

            case START_TIME:
              ColumnData startTimeColumnData = new ColumnData();
              startTimeColumnData.setValue(userPerformancePerDate.getStartTime());
              return new SimpleEntry<>(START_TIME, startTimeColumnData);

            case END_TIME:
              ColumnData endTimeColumnData = new ColumnData();
              endTimeColumnData.setValue(userPerformancePerDate.getEndTime());
              return new SimpleEntry<>(END_TIME, endTimeColumnData);

            case SPRAYED_DIFF:

              String sprayString = null;
              ColumnData sprayedDiffColumnData = new ColumnData();
              if (userPerformancePerDate.getSprayChecked().equals("")) {
                sprayString = "n/a";
              } else {
                int sprayDiff = userPerformancePerDate.getSprayDiff();
                sprayString = String.valueOf(sprayDiff);
              }
              sprayedDiffColumnData.setValue(sprayString);
              return new SimpleEntry<>(SPRAYED_DIFF, sprayedDiffColumnData);

            case FOUND_DIFF:
              String foundString = null;
              ColumnData foundDiffColumnData = new ColumnData();
              if (userPerformancePerDate.getFoundChecked().equals("")) {
                foundString = "n/a";
              } else {
                int foundDiff = userPerformancePerDate.getFoundDiff();
                foundString = String.valueOf(foundDiff);
              }

              foundDiffColumnData.setValue(foundString);
              return new SimpleEntry<>(FOUND_DIFF, foundDiffColumnData);

            case CHECKED:

              boolean foundChecked = true;
              boolean foundDiffBool = false;
              if (userPerformancePerDate.getFoundChecked().equals("")) {
                foundChecked = false;
              } else {
                int foundDiff = userPerformancePerDate.getFoundDiff();
                if (foundDiff == 0) {
                  foundDiffBool = true;
                }
              }

              boolean sprayChecked = true;
              boolean sprayDiffBool = false;
              if (userPerformancePerDate.getSprayChecked().equals("")) {
                sprayChecked = false;
              } else {
                int sprayDiff = userPerformancePerDate.getSprayDiff();
                if (sprayDiff == 0) {
                  sprayDiffBool = true;
                }
              }

              String checked = "match";
              if (!sprayChecked || !foundChecked) {
                checked = "No Summary Captured";
              } else {
                if (!foundDiffBool) {
                  checked = "Found Difference";
                  if (!sprayDiffBool) {
                    checked = "Found and Spray Difference";
                  }
                } else {
                  if (!sprayDiffBool) {
                    checked = "Spray Difference";
                  }
                }
              }

              ColumnData checkedColumnData = new ColumnData();
              checkedColumnData.setValue(checked);
              return new SimpleEntry<>(CHECKED, checkedColumnData);

            default:
              return new SimpleEntry<>(column, new ColumnData());
          }
        }).collect(Collectors.toCollection(LinkedList::new));
  }


  public List<RowData> getRowData(Plan plan, String parentUserLevelId, boolean startAtTop) {

    List<UserPerformanceDistrictAggregateProjection> allDistrictAggregatedStats = null;
    List<UserPerformanceDistrictAggregateProjection> allDistrictDaysWorkedAggregatedStats = null;
    List<UserPerformanceDistrictAggregateProjection> allDistrictHoursWorkedAggregateStats = null;
    Map<String, Integer> districtDaysWorkedMap = null;
    Map<String, Map<String, String>> hoursWorkedMap = null;
    String typeString = null;
    if (startAtTop) {
      typeString = "district";
      allDistrictAggregatedStats = performanceEventTrackerRepository.getAllDistrictAggregatedStats(
          plan.getIdentifier());
      allDistrictDaysWorkedAggregatedStats = performanceEventTrackerRepository
          .getAllDistrictDaysWorkedAggregatedStats(plan.getIdentifier());
      allDistrictHoursWorkedAggregateStats = performanceEventTrackerRepository
          .getAllDistrictHoursWorkedAggregateStats(plan.getIdentifier());

      districtDaysWorkedMap = allDistrictDaysWorkedAggregatedStats.stream()
          .collect(Collectors.toMap(UserPerformanceDistrictAggregateProjection::getDistrict,
              UserPerformanceDistrictAggregateProjection::getDaysWorked));

      hoursWorkedMap = allDistrictHoursWorkedAggregateStats.stream()
          .collect(Collectors.toMap(UserPerformanceDistrictAggregateProjection::getDistrict,
              districtHoursWorkedAggregateStats -> Map.of("AverageStartTime",
                  districtHoursWorkedAggregateStats.getAverageStartTime(), "AverageEndTime",
                  districtHoursWorkedAggregateStats.getAverageEndTime(), "AverageDuration",
                  districtHoursWorkedAggregateStats.getAverageDuration())));

    } else {
      PerformanceUserType performanceUserTypeByPlanIdentifierAndUser = performanceUserTypeRepository.findPerformanceUserTypeByPlanIdentifierAndUserString(
          plan.getIdentifier(), parentUserLevelId);

      if (performanceUserTypeByPlanIdentifierAndUser != null) {

        switch (performanceUserTypeByPlanIdentifierAndUser.getTypeString()) {
          case "district":
            typeString = "deviceUser";
            allDistrictAggregatedStats = performanceEventTrackerRepository.getIndividualDistrictAggregatedStats(
                plan.getIdentifier(), parentUserLevelId);
            allDistrictDaysWorkedAggregatedStats = performanceEventTrackerRepository.getIndividualDistrictDaysWorkedAggregatedStats(
                plan.getIdentifier(), parentUserLevelId);
            allDistrictHoursWorkedAggregateStats = performanceEventTrackerRepository.getIndividualDistrictHoursWorkedAggregateStats(
                plan.getIdentifier(), parentUserLevelId);

            List<UserPerformanceDistrictAggregateProjection> districtStats = performanceEventTrackerRepository.getDistrictStats(
                plan.getIdentifier(),
                parentUserLevelId);

            districtDaysWorkedMap = allDistrictDaysWorkedAggregatedStats.stream()
                .collect(Collectors.toMap(UserPerformanceDistrictAggregateProjection::getDeviceUser,
                    UserPerformanceDistrictAggregateProjection::getDaysWorked));

            hoursWorkedMap = allDistrictHoursWorkedAggregateStats.stream()
                .collect(Collectors.toMap(UserPerformanceDistrictAggregateProjection::getDeviceUser,
                    districtHoursWorkedAggregateStats -> Map.of("AverageStartTime",
                        districtHoursWorkedAggregateStats.getAverageStartTime(), "AverageEndTime",
                        districtHoursWorkedAggregateStats.getAverageEndTime(), "AverageDuration",
                        districtHoursWorkedAggregateStats.getAverageDuration())));

            break;
          case "deviceUser":
          case "fieldWorker":
          default:
            typeString = "fieldWorker";
            allDistrictAggregatedStats = performanceEventTrackerRepository.getIndividualDeviceUserAggregatedStats(
                plan.getIdentifier(), parentUserLevelId);
            allDistrictDaysWorkedAggregatedStats = performanceEventTrackerRepository.getIndividualDeviceUserDaysWorkedAggregatedStats(
                plan.getIdentifier(), parentUserLevelId);
            allDistrictHoursWorkedAggregateStats = performanceEventTrackerRepository.getIndividualDeviceUserHoursWorkedAggregateStats(
                plan.getIdentifier(), parentUserLevelId);

            districtDaysWorkedMap = allDistrictDaysWorkedAggregatedStats.stream()
                .collect(
                    Collectors.toMap(UserPerformanceDistrictAggregateProjection::getFieldWorker,
                        UserPerformanceDistrictAggregateProjection::getDaysWorked));

            hoursWorkedMap = allDistrictHoursWorkedAggregateStats.stream()
                .collect(
                    Collectors.toMap(UserPerformanceDistrictAggregateProjection::getFieldWorker,
                        districtHoursWorkedAggregateStats -> Map.of("AverageStartTime",
                            districtHoursWorkedAggregateStats.getAverageStartTime(),
                            "AverageEndTime",
                            districtHoursWorkedAggregateStats.getAverageEndTime(),
                            "AverageDuration",
                            districtHoursWorkedAggregateStats.getAverageDuration())));

            break;


        }
      }
    }

    Map<String, Integer> finalDistrictDaysWorkedMap = districtDaysWorkedMap;
    Map<String, Map<String, String>> finalHoursWorkedMap = hoursWorkedMap;
    String finalTypeString = typeString;
    List<RowData> rows = allDistrictAggregatedStats.stream().map(districtAggregatedStats -> {

      int sprayed = districtAggregatedStats.getSprayed();

      int found = districtAggregatedStats.getFound();
      int notSprayed = districtAggregatedStats.getNotSprayed();
      int notSprayedRefused = districtAggregatedStats.getNotSprayedRefused();
      int sachets = districtAggregatedStats.getSachetCount();
      String name = null;
      List<UserPerformanceDistrictAggregateProjection> districtQualityChecks = null;
      String label = "";
      switch (finalTypeString) {
        case "district":
          name = districtAggregatedStats.getDistrict();
          label = "District";
          districtQualityChecks = performanceEventTrackerRepository.getDistrictQualityChecks(
              plan.getIdentifier(), name);
          break;
        case "deviceUser":
          name = districtAggregatedStats.getDeviceUser();
          label = "Supervisor";
          districtQualityChecks = performanceEventTrackerRepository.getDeviceUserQualityChecks(
              plan.getIdentifier(), name);
          break;
        case "fieldWorker":
          name = districtAggregatedStats.getFieldWorker();
          label = "SOP";
          break;
      }

      Map<String, ColumnData> columnData = new LinkedHashMap<>();
      ColumnData averageStructuresColumnData = new ColumnData();
      Double averageStructures = null;
      if (finalDistrictDaysWorkedMap.get(name) > 0) {
        averageStructures = (double) sprayed / (double) finalDistrictDaysWorkedMap.get(name);
        averageStructuresColumnData.setMeta("Sprayed: "+sprayed+" / "+"Days: "+finalDistrictDaysWorkedMap.get(name));
      } else {
        averageStructures = 0D;
      }

      averageStructuresColumnData.setValue(averageStructures);
      averageStructuresColumnData.setIsPercentage(true);
      columnData.put(AVERAGE_STRUCTURES, averageStructuresColumnData);

      ColumnData averageHoursWorkedColumnData = new ColumnData();
      String averageDuration = "";
      if (finalHoursWorkedMap.get(name) != null) {
        averageDuration = finalHoursWorkedMap.get(name).get("AverageDuration");
      }
      averageHoursWorkedColumnData.setValue(averageDuration);
      columnData.put(AVERAGE_HOURS_WORKED, averageHoursWorkedColumnData);

      ColumnData averageStartTimeColumnData = new ColumnData();
      String averageStartTime = "";
      if (finalHoursWorkedMap.get(name) != null) {
        averageStartTime = finalHoursWorkedMap.get(name).get("AverageStartTime");
      }
      averageStartTimeColumnData.setValue(averageStartTime);
      columnData.put(AVERAGE_START_TIME, averageStartTimeColumnData);

      ColumnData averageEndTimeColumnData = new ColumnData();
      String averageEndTime = "";
      if (finalHoursWorkedMap.get(name) != null) {
        averageEndTime = finalHoursWorkedMap.get(name).get("AverageEndTime");
      }
      averageEndTimeColumnData.setValue(averageEndTime);
      columnData.put(AVERAGE_END_TIME, averageEndTimeColumnData);

      String colName = "";
      if (finalTypeString.equals("district")) {
        colName = NUMBER_OF_DAYS_TEAMS_IN_THE_FIELD;
      } else {
        colName = DAYS_WORKED;
      }
      ColumnData daysWorkedColumnData = new ColumnData();
      daysWorkedColumnData.setValue(finalDistrictDaysWorkedMap.get(name));
      columnData.put(colName, daysWorkedColumnData);

      ColumnData foundStructure = new ColumnData();
      foundStructure.setValue(found);
      columnData.put(FOUND, foundStructure);

      ColumnData sprayedStructure = new ColumnData();
      sprayedStructure.setValue(sprayed);
      columnData.put(SPRAYED, sprayedStructure);

      ColumnData notSprayedCol = new ColumnData();
      notSprayedCol.setValue(notSprayed);
      columnData.put(NOT_SPRAYED, notSprayedCol);

      ColumnData notSprayedRefusedCol = new ColumnData();
      notSprayedRefusedCol.setValue(notSprayedRefused);
      columnData.put(NOT_SPRAYED_REFUSED, notSprayedRefusedCol);

      if (!finalTypeString.equals("deviceUser")) {
        ColumnData bottlesUsageRate = new ColumnData();
        Double sachetUsageRate = null;
        if (sachets > 0) {
          sachetUsageRate =  (double) sprayed / (double) sachets;
          bottlesUsageRate.setMeta("Sprayed: " + sprayed + " / Sachets: " + sachets);
        } else {
          sachetUsageRate = 0D;
        }
        bottlesUsageRate.setValue(sachetUsageRate);
        bottlesUsageRate.setIsPercentage(true);

        columnData.put(BOTTLES_USAGE_RATE, bottlesUsageRate);
      }

      if (!finalTypeString.equals("fieldWorker")) {
        ColumnData sprayedDiff = new ColumnData();
        boolean checked = false;
        if (districtQualityChecks != null && !districtQualityChecks.isEmpty()) {
          if (districtQualityChecks.get(0) != null) {
            checked = districtQualityChecks.get(0).getChecked();
          }
        }
        sprayedDiff.setValue(String.valueOf(checked));
        columnData.put(DATA_QUALITY_CHECK, sprayedDiff);
      }

      RowData rowData = new RowData();
      rowData.setColumnDataMap(columnData);
      rowData.setUserName(name);
      rowData.setUserId(name);
      rowData.setUserLabel(label);
      rowData.setUserType(name);
      rowData.setUserParent(parentUserLevelId);
      return rowData;
    }).collect(Collectors.toList());

    return rows.stream().peek(row -> {

      Map<String, ColumnData> collect = columnOrder.get(finalTypeString).stream()
          .map(col -> {
            Entry<String, ColumnData> stringColumnDataEntry = row.getColumnDataMap().entrySet()
                .stream()
                .filter(colData -> colData.getKey().equals(col)).findFirst().get();
            return stringColumnDataEntry;
          })
          .collect(Collectors.toMap(Entry::getKey,
              Entry::getValue, (a, b) -> b, LinkedHashMap::new));
      row.setColumnDataMap(collect);

    }).collect(Collectors.toList());

  }


}
