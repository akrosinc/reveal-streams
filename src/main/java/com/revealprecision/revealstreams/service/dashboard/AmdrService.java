package com.revealprecision.revealstreams.service.dashboard;

import static com.revealprecision.revealstreams.service.dashboard.DashboardService.ALL_OTHER_LEVELS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revealprecision.revealstreams.dto.AmdrFeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.dto.amdr.AmdrLandPageResponse;
import com.revealprecision.revealstreams.enums.amdr.AmdrColumnType;
import com.revealprecision.revealstreams.factory.LocationResponseFactory;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.models.amdr.AmdrChartData;
import com.revealprecision.revealstreams.models.amdr.AmdrColumnData;
import com.revealprecision.revealstreams.models.amdr.AmdrCounters;
import com.revealprecision.revealstreams.models.amdr.AmdrDataLocation;
import com.revealprecision.revealstreams.models.amdr.AmdrLandingPageData;
import com.revealprecision.revealstreams.models.amdr.AmdrMarkerStats;
import com.revealprecision.revealstreams.models.amdr.AmdrTotalsLandingPageData;
import com.revealprecision.revealstreams.models.amdr.AmdrTotalsPercentageLandingPageData;
import com.revealprecision.revealstreams.models.amdr.BarTrace;
import com.revealprecision.revealstreams.models.amdr.ColorMarker;
import com.revealprecision.revealstreams.models.amdr.Font;
import com.revealprecision.revealstreams.models.amdr.Layout;
import com.revealprecision.revealstreams.models.amdr.Legend;
import com.revealprecision.revealstreams.models.amdr.Line;
import com.revealprecision.revealstreams.models.amdr.LineMarker;
import com.revealprecision.revealstreams.models.amdr.LineTrace;
import com.revealprecision.revealstreams.models.amdr.Title;
import com.revealprecision.revealstreams.models.amdr.Trace;
import com.revealprecision.revealstreams.models.amdr.Xaxis;
import com.revealprecision.revealstreams.models.amdr.Yaxis;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.LocationHierarchy;
import com.revealprecision.revealstreams.persistence.domain.LocationRelationship;
import com.revealprecision.revealstreams.persistence.domain.amdr.AmdrData;
import com.revealprecision.revealstreams.persistence.domain.amdr.AmdrHeaderNames;
import com.revealprecision.revealstreams.persistence.domain.amdr.AmdrMappings;
import com.revealprecision.revealstreams.persistence.domain.amdr.HslColor;
import com.revealprecision.revealstreams.persistence.projection.LocationNameProjection;
import com.revealprecision.revealstreams.persistence.projection.amdr.AmdrDataCalcMatProjection;
import com.revealprecision.revealstreams.persistence.projection.amdr.LandingPageCountsProjection;
import com.revealprecision.revealstreams.persistence.repository.LocationHierarchyRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationRelationshipRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationRepository;
import com.revealprecision.revealstreams.persistence.repository.amdr.AmdrDataCalMatRepository;
import com.revealprecision.revealstreams.persistence.repository.amdr.AmdrHeaderNamesRepository;
import com.revealprecision.revealstreams.persistence.repository.amdr.AmdrMappingsRepository;
import com.revealprecision.revealstreams.persistence.repository.amdr.AmdrRepository;
import com.revealprecision.revealstreams.props.amdr.AmdrReportColumnProperties;
import com.revealprecision.revealstreams.service.LocationService;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmdrService {

  private final LocationHierarchyRepository locationHierarchyRepository;
  private final LocationService locationService;
  private final AmdrRepository amdrRepository;
  private final LocationRelationshipRepository locationRelationshipRepository;
  private final AmdrHeaderNamesRepository amdrHeaderNamesRepository;
  private final LocationRepository locationRepository;

  private final AmdrReportColumnProperties amdrReportColumnProperties;

  private final ObjectMapper objectMapper;

  private final AmdrDataCalMatRepository amdrDataCalMatRepository;

  private final AmdrMappingsRepository amdrMappingsRepository;
  public AmdrLandPageResponse getLandingPageData3() {

    List<LandingPageCountsProjection> rcdCountsByYearMonth = amdrRepository.getRCDCountsByYearMonth();
    List<LandingPageCountsProjection> passiveCountsByYearMonth = amdrRepository.getPassiveCountsByYearMonth();
    List<LandingPageCountsProjection> parasitologyCountsByYearMonth = amdrRepository.getParasitologyCountsByYearMonth();
    List<LandingPageCountsProjection> importCountsByYearMonth = amdrRepository.getImportCountsByYearMonth();

    Set<YearMonth> rcdYearMonth = rcdCountsByYearMonth.stream()
        .map(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()))
        .collect(Collectors.toSet());

    Set<YearMonth> passiveYearMonth = passiveCountsByYearMonth.stream()
        .map(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()))
        .collect(Collectors.toSet());

    Set<YearMonth> parasitologyYearMonth = parasitologyCountsByYearMonth.stream()
        .map(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()))
        .collect(Collectors.toSet());

    Set<YearMonth> importYearMonth = importCountsByYearMonth.stream()
        .map(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()))
        .collect(Collectors.toSet());

    Map<YearMonth, LandingPageCountsProjection> rcdMap = rcdCountsByYearMonth.stream().collect(
        Collectors.toMap(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()),
            rcdCount -> rcdCount, (a, b) -> b));

    Map<YearMonth, LandingPageCountsProjection> passiveMap = passiveCountsByYearMonth.stream()
        .collect(
            Collectors.toMap(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()),
                rcdCount -> rcdCount, (a, b) -> b));

    Map<YearMonth, LandingPageCountsProjection> parasitologyMap = parasitologyCountsByYearMonth.stream()
        .collect(
            Collectors.toMap(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()),
                rcdCount -> rcdCount, (a, b) -> b));

    Map<YearMonth, LandingPageCountsProjection> importMap = importCountsByYearMonth.stream()
        .collect(
            Collectors.toMap(rcdCount -> YearMonth.of(rcdCount.getYear(), rcdCount.getMonth()),
                rcdCount -> rcdCount, (a, b) -> b));

    Set<YearMonth> yearMonths = new HashSet<>();
    yearMonths.addAll(rcdYearMonth);
    yearMonths.addAll(parasitologyYearMonth);
    yearMonths.addAll(passiveYearMonth);
    yearMonths.addAll(importYearMonth);

    List<AmdrLandingPageData> sortedList = yearMonths.stream().map(yearMonth -> {
          AmdrLandingPageData data = new AmdrLandingPageData();
          if (rcdMap.containsKey(yearMonth)) {

            LandingPageCountsProjection rcdlandingPageCountsProjection = rcdMap.get(yearMonth);
            data.setRcdCases(rcdlandingPageCountsProjection.getCnt());
            if (data.getYearMonth() == null) {
              data.setYearMonth(yearMonth);
            }
          }
          if (passiveMap.containsKey(yearMonth)) {
            LandingPageCountsProjection rcdlandingPageCountsProjection = passiveMap.get(yearMonth);
            data.setPassiveCases(rcdlandingPageCountsProjection.getCnt());
            if (data.getYearMonth() == null) {
              data.setYearMonth(yearMonth);
            }
          }
          if (parasitologyMap.containsKey(yearMonth)) {
            LandingPageCountsProjection rcdlandingPageCountsProjection = parasitologyMap.get(yearMonth);
            data.setParasitologyReports(rcdlandingPageCountsProjection.getCnt());
            if (data.getYearMonth() == null) {
              data.setYearMonth(yearMonth);
            }
          }
          if (importMap.containsKey(yearMonth)) {
            LandingPageCountsProjection rcdlandingPageCountsProjection = importMap.get(yearMonth);
            data.setImportedSequences(rcdlandingPageCountsProjection.getCnt());
            if (data.getYearMonth() == null) {
              data.setYearMonth(yearMonth);
            }
          }
          return data;

        }).sorted(Comparator.comparing(AmdrLandingPageData::getYearMonth)) // sort by YearMonth
        .collect(Collectors.toList());// collect into a List

    int rcdTotal = rcdCountsByYearMonth.stream().mapToInt(LandingPageCountsProjection::getCnt)
        .sum();
    int passiveTotal = passiveCountsByYearMonth.stream()
        .mapToInt(LandingPageCountsProjection::getCnt).sum();
    int parasitologyTotal = parasitologyCountsByYearMonth.stream()
        .mapToInt(LandingPageCountsProjection::getCnt).sum();
    int importTotal = importCountsByYearMonth.stream().mapToInt(LandingPageCountsProjection::getCnt)
        .sum();

    AmdrTotalsLandingPageData amdrTotalsLandingPageData = new AmdrTotalsLandingPageData();
    amdrTotalsLandingPageData.setCases(rcdTotal + passiveTotal);
    amdrTotalsLandingPageData.setRcdCases(rcdTotal);
    amdrTotalsLandingPageData.setImportedSequences(importTotal);
    amdrTotalsLandingPageData.setPassiveCases(passiveTotal);
    amdrTotalsLandingPageData.setParasitologyReports(parasitologyTotal);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
    List<String> yearMonthStrings = sortedList.stream()
        .map(AmdrLandingPageData::getYearMonth)
        .map(yearMonth -> yearMonth.format(formatter))
        .map(mmmyyyy -> mmmyyyy.replaceAll(" ", "<BR>"))
        .collect(Collectors.toList());

    List<String> dates = sortedList.stream().map(AmdrLandingPageData::getYearMonth)
        .map(yearMonth -> LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1))
        .map(LocalDate::toString).collect(
            Collectors.toList());

    List<String> dates3 = sortedList.stream().map(AmdrLandingPageData::getYearMonth)
        .map(yearMonth -> LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 5))
        .map(LocalDate::toString).collect(
            Collectors.toList());

    List<String> dates5 = sortedList.stream().map(AmdrLandingPageData::getYearMonth)
        .map(yearMonth -> LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 9))
        .map(LocalDate::toString).collect(
            Collectors.toList());

    int width = 24 * 60 * 60 * 1000 * 2;

    BarTrace parasitologyRcd = new BarTrace();
    parasitologyRcd.setType("bar");
    parasitologyRcd.setName("Reactive cases (in-field)");
    parasitologyRcd.setWidth(width);
    parasitologyRcd.setX(dates);
    parasitologyRcd.setMarker(new ColorMarker("lightblue"));
    parasitologyRcd.setHovertemplate(""
        + "Reactive cases (in-field): %{customdata[0]}"
        + "<br>Passive HF Cases: %{customdata[2]}"
        + "<br>Total cases:  %{customdata[1]}<extra></extra>"
    );

    BarTrace parasitologyPassive = new BarTrace();
    parasitologyPassive.setType("bar");
    parasitologyPassive.setName("Passive HF Cases");
    parasitologyPassive.setWidth(width);
    parasitologyPassive.setX(dates);
    parasitologyPassive.setMarker(new ColorMarker("blue"));
    parasitologyPassive.setHovertemplate(""
        + "Reactive cases (in-field): %{customdata[2]}"
        + "<br>Passive HF Cases: %{customdata[0]}"
        + "<br>Total cases:  %{customdata[1]}<extra></extra>"
    );

    BarTrace importsRcd = new BarTrace();
    importsRcd.setType("bar");
    importsRcd.setName("Reactive cases (in-field)");
    importsRcd.setWidth(width);
    importsRcd.setX(dates);
    importsRcd.setMarker(new ColorMarker("lightblue"));
    importsRcd.setHovertemplate(""
        + "Reactive cases (in-field): %{customdata[0]}"
        + "<br>Passive HF Cases: %{customdata[2]}"
        + "<br>Total cases:  %{customdata[1]}<extra></extra>"
    );

    BarTrace importsPassive = new BarTrace();
    importsPassive.setType("bar");
    importsPassive.setName("Passive HF Cases");
    importsPassive.setWidth(width);
    importsPassive.setX(dates);
    importsPassive.setMarker(new ColorMarker("blue"));
    importsPassive.setHovertemplate(""
        + "Reactive cases (in-field): %{customdata[2]}"
        + "<br>Passive HF Cases: %{customdata[0]}"
        + "<br>total incoming cases:  %{customdata[1]}<extra></extra>"
    );

    BarTrace parasitology = new BarTrace();
    parasitology.setType("bar");
    parasitology.setName("Parasitology reports");
    parasitology.setWidth(width);
    parasitology.setX(dates3);
    parasitology.setMarker(new ColorMarker("lightgreen"));
    parasitology.setHovertemplate(
        ""
            + "Parasitology reports: %{customdata[0]}<extra></extra>"
    );

    BarTrace imports = new BarTrace();
    imports.setType("bar");
    imports.setName("Genomic Sequence Imports");
    imports.setWidth(width);
    imports.setX(dates3);
    imports.setMarker(new ColorMarker("green"));
    imports.setHovertemplate(""
        + "Genomic Sequence Imports: %{customdata[0]}"
        + "<br>accumulated cases: %{y}<extra></extra>");

    BarTrace parasitologyParasitology = new BarTrace();
    parasitologyParasitology.setType("bar");
    parasitologyParasitology.setName("Parasitology reports");
    parasitologyParasitology.setWidth(width);
    parasitologyParasitology.setX(dates);
    parasitologyParasitology.setMarker(new ColorMarker("lightgreen"));
    parasitologyParasitology.setHovertemplate(
        "Parasitology reports: %{customdata[0]}<extra></extra>");

    BarTrace parasitologyImport = new BarTrace();
    parasitologyImport.setType("bar");
    parasitologyImport.setName("Genomic Sequence Imports");
    parasitologyImport.setWidth(width);
    parasitologyImport.setX(dates5);
    parasitologyImport.setMarker(new ColorMarker("green"));
    parasitologyImport.setHovertemplate(
        "Genomic Sequence Imports: %{customdata[0]}<extra></extra>");

    LineTrace parasitologyScatter = new LineTrace();
    parasitologyScatter.setType("scatter");
    parasitologyScatter.setName("Total cases minus Parasitology Reports");
    parasitologyScatter.setLine(new Line("red", 1));
    parasitologyScatter.setMode("lines+markers");
    parasitologyScatter.setX(dates3);
    parasitologyScatter.setMarker(new LineMarker(3));
    parasitologyScatter.setHovertemplate(
        "Total cases minus Parasitology Reports: %{y}<extra></extra>");

    LineTrace importScatter = new LineTrace();
    importScatter.setType("scatter");
    importScatter.setName("balance after import");
    importScatter.setLine(new Line("darkred", 1));
    importScatter.setMode("lines+markers");
    importScatter.setX(dates3);
    importScatter.setMarker(new LineMarker(3));
    importScatter.setHovertemplate("imports: %{y}<extra></extra>");

    LineTrace paraImportScatter = new LineTrace();
    paraImportScatter.setType("scatter");
    paraImportScatter.setName("Parasitology reports minus Genomic Sequence Imports");
    paraImportScatter.setLine(new Line("darkred", 1));
    paraImportScatter.setMode("lines+markers");
    paraImportScatter.setX(dates3);
    paraImportScatter.setMarker(new LineMarker(3));
    paraImportScatter.setHovertemplate(
        "Parasitology reports minus Genomic Sequence Imports: %{y}<extra></extra>");

    Integer parasitologyBase = 0;
    Integer importBase = 0;
    Integer paraToImportBase = 0;
    for (AmdrLandingPageData landingPage : sortedList) {

      Integer rcdCases = landingPage.getRcdCases() == null ? 0 : landingPage.getRcdCases();
      Integer passiveCases =
          landingPage.getPassiveCases() == null ? 0 : landingPage.getPassiveCases();
      Integer parasitologyReports =
          landingPage.getParasitologyReports() == null ? 0 : landingPage.getParasitologyReports();
      Integer importedSequences =
          landingPage.getImportedSequences() == null ? 0 : landingPage.getImportedSequences();

      parasitologyRcd.getY().add(rcdCases);
      parasitologyRcd.getBase().add(0);
      List<Integer> parasitologyRcdcustom = new ArrayList<>();
      parasitologyRcdcustom.add(rcdCases);
      parasitologyRcdcustom.add(rcdCases + passiveCases);
      parasitologyRcdcustom.add(passiveCases);
      parasitologyRcd.getCustomdata().add(parasitologyRcdcustom);

      importsRcd.getY().add(rcdCases);
      importsRcd.getBase().add(0);
      List<Integer> importsRcdcustom = new ArrayList<>();
      importsRcdcustom.add(rcdCases);
      importsRcdcustom.add(rcdCases + passiveCases);
      importsRcdcustom.add(passiveCases);
      importsRcd.getCustomdata().add(importsRcdcustom);

      parasitologyBase += rcdCases;
      importBase += rcdCases;

      parasitologyPassive.getY().add(passiveCases);
      parasitologyPassive.getBase().add(rcdCases);
      List<Integer> parasitologyPassivecustom = new ArrayList<>();
      parasitologyPassivecustom.add(passiveCases);
      parasitologyPassivecustom.add(rcdCases + passiveCases);
      parasitologyPassivecustom.add(rcdCases);
      parasitologyPassive.getCustomdata().add(parasitologyPassivecustom);

      importsPassive.getY().add(passiveCases);
      importsPassive.getBase().add(rcdCases);
      List<Integer> importsPassivecustom = new ArrayList<>();
      importsPassivecustom.add(passiveCases);
      importsPassivecustom.add(rcdCases + passiveCases);
      importsPassivecustom.add(rcdCases);
      importsPassive.getCustomdata().add(importsPassivecustom);

      parasitologyBase += passiveCases;
      importBase += passiveCases;

      int parasitologyYVal = parasitologyReports * -1;
      parasitology.getY().add(parasitologyReports);
      parasitology.getBase().add(0);
      List<Integer> parasitologycustom = new ArrayList<>();
      parasitologycustom.add(parasitologyReports);
      parasitology.getCustomdata().add(parasitologycustom);

      int importsYVal = importedSequences * -1;
      imports.getY().add(importedSequences);
      imports.getBase().add(0);
      List<Integer> importscustom = new ArrayList<>();
      importscustom.add(importedSequences);
      imports.getCustomdata().add(importscustom);

      parasitologyParasitology.getY().add(parasitologyReports);
      parasitologyParasitology.getBase().add(0);
      List<Integer> parasitologyParasitologycustom = new ArrayList<>();
      parasitologyParasitologycustom.add(parasitologyReports);
      parasitologyParasitology.getCustomdata().add(parasitologyParasitologycustom);

      paraToImportBase += parasitologyReports;

      int paraImportsYVal = importedSequences * -1;
      parasitologyImport.getY().add(importedSequences);
      parasitologyImport.getBase().add(0);
      List<Integer> parasitologyImportcustom = new ArrayList<>();
      parasitologyImportcustom.add(importedSequences);
      parasitologyImport.getCustomdata().add(parasitologyImportcustom);

      parasitologyBase += parasitologyYVal;
      importBase += importsYVal;
      paraToImportBase += importsYVal;

      parasitologyScatter.getY().add(parasitologyBase);
      List<Integer> parasitologyScattercustom = new ArrayList<>();
      parasitologyScattercustom.add(parasitologyReports);
      parasitologyScatter.getCustomdata().add(parasitologyScattercustom);

      importScatter.getY().add(importBase);
      List<Integer> importScattercustom = new ArrayList<>();
      importScattercustom.add(importedSequences);
      importScatter.getCustomdata().add(importScattercustom);

      paraImportScatter.getY().add(paraToImportBase);
      List<Integer> paraImportScattercustom = new ArrayList<>();
      paraImportScattercustom.add(importedSequences);
      paraImportScatter.getCustomdata().add(paraImportScattercustom);

    }
    List<Trace> parasitologyTraces = List.of(parasitologyRcd, parasitologyPassive, parasitology,
        parasitologyImport,
        parasitologyScatter, paraImportScatter);
    List<Trace> importTraces = List.of(importsRcd, importsPassive, imports, importScatter);
    List<Trace> paraImportTraces = List.of(parasitologyParasitology, parasitologyImport,
        paraImportScatter);

    Xaxis xaxis = new Xaxis();
    xaxis.setTickVals(dates);
    xaxis.setTickText(yearMonthStrings);
    xaxis.setTickFont(new Font(10));

    xaxis.setFixedRange(true);

    Yaxis yaxis = new Yaxis();
    Title xAxisTitle = new Title("Cases", null, new Font(11));
    yaxis.setTitle(xAxisTitle);
    yaxis.setTickFont(new Font(10));

    Layout parasitologylayout = new Layout();
    parasitologylayout.setBarGap(5);
    parasitologylayout.setBarMode("overlay");
    parasitologylayout.setXaxis(xaxis);
    parasitologylayout.setYaxis(yaxis);
    Title parasitologyTitle = new Title();
    parasitologyTitle.setText("Total Cases (by month) vs Parasitology Reports");
    parasitologyTitle.setFont(new Font(11));
    parasitologylayout.setTitle(parasitologyTitle);
    parasitologylayout.setLegend(new Legend(new Font(10)));

    Layout importLayout = new Layout();
    importLayout.setBarGap(5);
    importLayout.setBarMode("overlay");
    importLayout.setXaxis(xaxis);
    importLayout.setYaxis(yaxis);
    Title importTitle = new Title();
    importTitle.setText("Total Cases (by month) vs Sequence Imports");
    importTitle.setFont(new Font(11));
    importLayout.setTitle(importTitle);
    importLayout.setLegend(new Legend(new Font(10)));

    Layout paraImportLayout = new Layout();
    paraImportLayout.setBarGap(5);
    paraImportLayout.setBarMode("overlay");
    paraImportLayout.setXaxis(xaxis);
    paraImportLayout.setYaxis(yaxis);
    Title paraImportTitle = new Title();
    paraImportTitle.setText("Parasitology Reports (by month) vs Sequence Imports");
    paraImportTitle.setFont(new Font(11));
    paraImportLayout.setTitle(paraImportTitle);
    paraImportLayout.setLegend(new Legend(new Font(10)));

    AmdrChartData parasitologyData = new AmdrChartData();
    parasitologyData.setData(parasitologyTraces);
    parasitologyData.setLayout(parasitologylayout);

    AmdrChartData importData = new AmdrChartData();
    importData.setData(importTraces);
    importData.setLayout(importLayout);

    AmdrChartData parasitologyImportData = new AmdrChartData();
    parasitologyImportData.setData(paraImportTraces);
    parasitologyImportData.setLayout(paraImportLayout);

    double importToCasesPercentage = (double) amdrTotalsLandingPageData.getImportedSequences()
        / (double) amdrTotalsLandingPageData.getCases();
    double parasitologyToCasesPercentage =
        (double) amdrTotalsLandingPageData.getParasitologyReports()
            / (double) amdrTotalsLandingPageData.getCases();
    double importToParasitologyPercentage =
        (double) amdrTotalsLandingPageData.getImportedSequences()
            / (double) amdrTotalsLandingPageData.getParasitologyReports();

    AmdrTotalsPercentageLandingPageData amdrTotalsPercentageLandingPageData = new AmdrTotalsPercentageLandingPageData();
    amdrTotalsPercentageLandingPageData.setImportToParasitologyPercentage(
        importToParasitologyPercentage);
    amdrTotalsPercentageLandingPageData.setParasitologyToCasesPercentage(
        parasitologyToCasesPercentage);
    amdrTotalsPercentageLandingPageData.setImportToCasesPercentage(importToCasesPercentage);

    AmdrLandPageResponse amdrLandPageResponse = new AmdrLandPageResponse();
    amdrLandPageResponse.setParasitologyData(parasitologyData);
    amdrLandPageResponse.setImportData(importData);
    amdrLandPageResponse.setParasitologyImportData(parasitologyImportData);
    amdrLandPageResponse.setAmdrTotalsLandingPageData(amdrTotalsLandingPageData);
    amdrLandPageResponse.setAmdrTotalsPercentageLandingPageData(
        amdrTotalsPercentageLandingPageData);

    return amdrLandPageResponse;
  }
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class HeaderName implements Serializable{
    private HslColor color;
    private String name;

  }

  public Map<AmdrColumnType,Map<String, HeaderName>> getHeadersForReport() {
    Map<AmdrColumnType, Map<String, HeaderName>> drugList = amdrHeaderNamesRepository.findAllByColType(
            AmdrColumnType.DRUG.name())
        .stream()
        .collect(Collectors.groupingBy(
            item -> AmdrColumnType.valueOf(item.getId().getColType()), // outer key
            Collectors.toMap(
                item -> item.getId().getKey(), // inner map key
                item -> HeaderName.builder()   // inner map value
                    .color(item.getColor())
                    .name(item.getName())
                    .build()
            )
        ));

     Map<String, HeaderName> haplotypeMap = amdrHeaderNamesRepository.findAllByColType(
            AmdrColumnType.DRUG.name())
        .stream()
         .filter(item -> !List.of("dhfr_dhps","crt_mdr1","dhfr").contains(item.getId().getKey()))
        .collect(// outer key
            Collectors.toMap(
                item -> item.getId().getKey(), // inner map key
                item -> HeaderName.builder()   // inner map value
                    .color(item.getColor())
                    .name(item.getId().getKey())
                    .build()
            )
        );

    Map<AmdrColumnType, Map<String, HeaderName>> haplotypeList =
        Map.of(AmdrColumnType.HAPLOTYPE, haplotypeMap);

    Map<AmdrColumnType, Map<String, HeaderName>> combinedMap = new HashMap<>();
    combinedMap.putAll(drugList);
    combinedMap.putAll(haplotypeList);

    return combinedMap;

  }

  public AmdrFeatureSetResponse getDataForReport(
      String parentIdentifierString, String clickedColumn) {

    List<AmdrHeaderNames> amdrHeaderNames = amdrHeaderNamesRepository.findAll();

    Map<String, String> amdrHeaderNameMap = amdrHeaderNames.stream()
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getName, (a, b) -> b));

    Map<String, HslColor> amdrHeaderColorMap = amdrHeaderNames.stream()
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getColor, (a, b) -> b));

    Location parentLocation = null;
    UUID parentIdentifier = null;

    if (parentIdentifierString != null) {
      try {
        parentIdentifier = UUID.fromString(parentIdentifierString);
      } catch (IllegalArgumentException illegalArgumentException) {
        parentIdentifier = UUID.fromString(parentIdentifierString.split("_")[2]);
      }
    }

    if (parentIdentifier != null) {
      parentLocation = locationService.findByIdentifier(parentIdentifier);
    }

    LocationHierarchy aDefault = locationHierarchyRepository.findLocationHierarchyByName("default");

    List<LocationRelationship> locationRelationshipsByParentLocation_identifierAndLocationHierarchy = locationRelationshipRepository.findLocationRelationshipsByParentLocation_IdentifierAndLocationHierarchy(
        parentIdentifier, aDefault);

    if (locationRelationshipsByParentLocation_identifierAndLocationHierarchy.isEmpty()) {
      return AmdrFeatureSetResponse.builder().noLocationData(true).noDashboardData(true).build();

    }

    Location finalParentLocation = parentLocation;
    List<PlanLocationDetails> collect = locationRelationshipsByParentLocation_identifierAndLocationHierarchy.stream()
        .map(LocationRelationship::getLocation)
        .map(location -> {
          PlanLocationDetails planLocations = new PlanLocationDetails();
          planLocations.setParentLocation(finalParentLocation);
          planLocations.setLocation(location);
          planLocations.setHasChildren(false);
          planLocations.setAssignedLocations(0L);
          planLocations.setChildrenNumber(0L);
          planLocations.setAssignedTeams(0L);
          return planLocations;
        })
        .collect(
            Collectors.toList());

    Map<UUID, RowData> rowDataMap = new HashMap<>();

    Long maxYear = amdrRepository.getLatestYear();

    List<List<PlanLocationDetails>> lists = splitList(collect
        , 500);

    List<RowData> rowData = getRows(parentLocation, collect, clickedColumn, amdrHeaderNameMap,
        amdrHeaderColorMap, maxYear, lists);

    List<Location> parent;
    if (parentIdentifier == null){
      parent = locationRelationshipRepository.getRootLocationDetailsByAndHierarchyId(
          aDefault.getIdentifier());
    } else {
      parent = List.of(finalParentLocation);
    }

    AmdrDataCalcMatProjection amdrDataCalcMatIdsBy = amdrDataCalMatRepository.getAmdrDataCalcMatIdsBy(
        parent.stream()
            .map(Location::getIdentifier)
            .collect(Collectors.toList()),
        List.of(Double.valueOf(maxYear), 2025d));
    Map<String,Map<String, AmdrMarkerStats>> markersMap;
    try {
    markersMap = objectMapper.readValue(
        amdrDataCalcMatIdsBy.getMarkers(),
                new TypeReference<>() {
                });
    } catch (Exception e) {
      markersMap = null;
    }

    if (rowData != null) {
      rowDataMap = rowData.stream()
          .collect(Collectors.toMap(RowData::getLocationIdentifier, row -> row, (a, b) -> b));
      if (rowData.size() == 0) {
        return AmdrFeatureSetResponse.builder().noDashboardData(true).build();
      }
    }
    return getFeatureSetResponse(parentIdentifier, collect,
        rowDataMap, ALL_OTHER_LEVELS, clickedColumn, markersMap);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class Accumulator {

    private Integer num = 0;
    private Integer denom = 0;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class TriAccumulator {

    private Double mono = 0d;
    private Double mixed = 0d;
    private Double total = 0d;
    private Double totalRecs = 0d;
  }

  public List<RowData> getRows(
      Location parentLocation, List<PlanLocationDetails> locationDetails, String clickedColumn,
      Map<String, String> amdrHeaderNameMap, Map<String, HslColor> amdrHeaderColorMap, Long maxYear,
      List<List<PlanLocationDetails>> lists ) {


    List<AmdrData> amdrDataStream = lists.stream()
        .flatMap(listOfLocationIds ->
            amdrRepository.findByLocationIdInAndCollectionYearIn(
                    listOfLocationIds.stream()
                        .map(locationDetail -> locationDetail.getLocation().getIdentifier())
                        .collect(Collectors.toList()),
                    List.of(String.valueOf(maxYear), "2025"))
                .stream()
        ).collect(Collectors.toList());


    if (maxYear != null) {
      if (clickedColumn != null) {

        List<AmdrMappings> allMappings = amdrMappingsRepository.findAll();

        Map<String, String> subkeyParentMap = allMappings.stream()
            .flatMap(mapItem -> mapItem.getAmdrSubKeys().entrySet().stream())
            .flatMap(subKeyEntry -> {
              List<String> subKeys = subKeyEntry.getValue();
              return subKeys.stream().map(item -> new SimpleEntry<>(item, subKeyEntry.getKey()));
            }).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> a));


          Set<UUID> collect2 = amdrDataStream.stream().map(AmdrData::getLocationId)
            .collect(Collectors.toSet());

        List<LocationNameProjection> byIdentifierIn = locationRepository.findLocationNamesByIdentifierIn(
            collect2);

        Map<String, String> locationNameMapById = byIdentifierIn.stream()
            .collect(Collectors.toMap(LocationNameProjection::getIdentifier,
                LocationNameProjection::getLocationName));

        List<AmdrDataLocation> amdrDataLocationList = amdrDataStream.stream()
            .map(amdrData -> AmdrDataLocation
                .builder()
                .data(amdrData.getData())
                .collectionYear(amdrData.getCollectionYear())
                .id(amdrData.getId())
                .datetime(amdrData.getDatetime())
                .locationId(amdrData.getLocationId())
                .overallObject(amdrData.getOverallObject())
                .type(amdrData.getType())
                .overallValue(amdrData.getOverallValue())
                .locationName(locationNameMapById.get(amdrData.getLocationId().toString()))
                .build()
            ).sorted(Comparator.comparing(AmdrDataLocation::getLocationName))
            .collect(Collectors.toList());

        Map<UUID, List<AmdrDataLocation>> amdrDataLocationListMap = amdrDataLocationList.stream()
            .collect(Collectors.groupingBy(AmdrDataLocation::getLocationId));

        Map<UUID, Map<String, Accumulator>> mainAccumulator = new HashMap<>();
        Map<UUID, Entry<String, Map<String, TriAccumulator>>> triAccumulator = new HashMap<>();

        for (Entry<UUID, List<AmdrDataLocation>> amdrDataLocationListEntry : amdrDataLocationListMap.entrySet()) {
          boolean checked = true;
          for (AmdrDataLocation amdrDataLocation : amdrDataLocationListEntry.getValue()) {

            for (Entry<String, Map<String, AmdrCounters>> amdrCounterMapEntry : amdrDataLocation.getData()
                .entrySet()) {
              String key = amdrCounterMapEntry.getKey();

              if (checked) {

                List<AmdrData> amdrDataList = amdrRepository.findByTypeAndLocationIdAndCollectionYearIn(
                    key, amdrDataLocationListEntry.getKey(),
                    List.of(String.valueOf(maxYear), "2025"));

                for (AmdrData amdrData1 : amdrDataList) {
                  int val = amdrData1.getOverallObject().getOverallValue();
                  int denom = amdrData1.getOverallObject().getOverallTotalRecs();

                  Map<String, Accumulator> stringAccumulatorMap = null;
                  if (mainAccumulator.containsKey(amdrDataLocationListEntry.getKey())) {
                    stringAccumulatorMap = mainAccumulator.get(
                        amdrDataLocationListEntry.getKey());
                  } else {
                    stringAccumulatorMap = new HashMap<>();
                  }
                  Accumulator accumulator = null;
                  if (stringAccumulatorMap.containsKey(key)) {
                    accumulator = stringAccumulatorMap.get(key);
                  } else {
                    accumulator = new Accumulator();
                  }

                  accumulator.setNum(accumulator.getNum() + val);
                  accumulator.setDenom(accumulator.getDenom() + denom);
                  stringAccumulatorMap.put(key, accumulator);
                  mainAccumulator.put(amdrDataLocationListEntry.getKey(), stringAccumulatorMap);
                }
              }

              for (Entry<String, AmdrCounters> amdrCountersEntry : amdrCounterMapEntry.getValue()
                  .entrySet()) {

                String key1 = amdrCountersEntry.getKey();

                Entry<String, Map<String, TriAccumulator>> triAccumulatorMapByOuterKey = null;
                if (triAccumulator.containsKey(amdrDataLocationListEntry.getKey())) {
                  triAccumulatorMapByOuterKey = triAccumulator.get(
                      amdrDataLocationListEntry.getKey());
                } else {
                  Map<String, TriAccumulator> stringTriAccumulatorMap = new HashMap<>();
                  SimpleEntry<String, Map<String, TriAccumulator>> stringMapSimpleEntry = new SimpleEntry<>(
                      key, stringTriAccumulatorMap);
                  triAccumulatorMapByOuterKey = stringMapSimpleEntry;
                }
                TriAccumulator triAccumulator1 = null;
                if (triAccumulatorMapByOuterKey.getValue().containsKey(key1)) {
                  triAccumulator1 = triAccumulatorMapByOuterKey.getValue().get(key1);
                } else {
                  triAccumulator1 = new TriAccumulator();
                }

                triAccumulator1.setMixed(
                    triAccumulator1.getMixed() + amdrCountersEntry.getValue().getMixed());
                triAccumulator1.setMono(
                    triAccumulator1.getMono() + amdrCountersEntry.getValue().getMono());
                triAccumulator1.setTotal(
                    triAccumulator1.getTotal() + amdrCountersEntry.getValue().getTotal());
                triAccumulator1.setTotalRecs(
                    triAccumulator1.getTotalRecs() + amdrCountersEntry.getValue().getTotalRecs());

                triAccumulatorMapByOuterKey.getValue().put(key1, triAccumulator1);

                triAccumulatorMapByOuterKey = new SimpleEntry<>(key,
                    triAccumulatorMapByOuterKey.getValue());

                triAccumulator.put(amdrDataLocationListEntry.getKey(), triAccumulatorMapByOuterKey);

              }

            }
            checked = false;
          }

        }

        List<RowData> collect = mainAccumulator.entrySet().stream().map(mainAccumulatorEntry -> {

          Map<String, Accumulator> value = mainAccumulatorEntry.getValue();

          Map<String, ColumnData> string = value.entrySet().stream().map(accumulatorEntry -> {

            Accumulator value1 = accumulatorEntry.getValue();

            int val = value1.getNum();
            int denom = value1.getDenom();
            double perc = denom > 0 ?
                (double) val
                    / (double) denom * 100 :
                (double) 0;
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.00", symbols);
            String totalVal = df.format(perc);
            String meta =
                "total recs: " + denom +
                    " \r\n - gene: " + val + " => "
                    + totalVal + "%";

            AmdrColumnData amdrColumnData = new AmdrColumnData();

            amdrColumnData.setValue(totalVal + " " + val);
            amdrColumnData.setMeta(meta);
            amdrColumnData.setDataType("string");
            amdrColumnData.setIsPercentage(true);
            amdrColumnData.setDescription(amdrHeaderNameMap.get(accumulatorEntry.getKey()));
            amdrColumnData.setHslColor(amdrHeaderColorMap.get(accumulatorEntry.getKey()));
            amdrColumnData.setAmdrParent(subkeyParentMap.get(accumulatorEntry.getKey()));



            return new SimpleEntry<>(accumulatorEntry.getKey(), amdrColumnData);

          }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b));

          Entry<String, Map<String, TriAccumulator>> stringMapEntry = triAccumulator.get(
              mainAccumulatorEntry.getKey());

          Map<String, ColumnData> string1 = stringMapEntry.getValue().entrySet().stream()
              .map(stringTriAccumulatorEntry -> {

                TriAccumulator value1 = stringTriAccumulatorEntry.getValue();

                double mono = value1.getMono() /
                    (value1.getTotalRecs() != null
                        && value1.getTotalRecs() > 0 ?
                        value1.getTotalRecs() : 1) * 100;
                double mixed = value1.getMixed() /
                    (value1.getTotalRecs() != null
                        && value1.getTotalRecs() > 0 ?
                        value1.getTotalRecs() : 1) * 100;
                double total = value1.getTotal() /
                    (value1.getTotalRecs() != null
                        && value1.getTotalRecs() > 0 ?
                        value1.getTotalRecs() : 1) * 100;
                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                DecimalFormat df = new DecimalFormat("0.00", symbols);
                String totalVal = df.format(total);
                String meta =
                    "total recs: " + value1.getTotalRecs() +
                        " \r\n - mono: " + value1.getMono() + " => "
                        + String.format(
                        "%.2f", mono) + "%"
                        + "\r\n - mixed: " + value1.getMixed() + " => "
                        + String.format(
                        "%.2f", mixed) + "%"
                        + "\r\n - total: " + value1.getTotal() + " => "
                        + totalVal + "%";

                AmdrColumnData amdrColumnData = new AmdrColumnData();

                amdrColumnData.setValue(totalVal + " " + total);
                amdrColumnData.setMeta(meta);
                amdrColumnData.setDataType("string");
                amdrColumnData.setIsPercentage(true);
                amdrColumnData.setHslColor(amdrHeaderColorMap.get(stringTriAccumulatorEntry.getKey()));
                amdrColumnData.setDescription(amdrHeaderNameMap.get(stringTriAccumulatorEntry.getKey()));

                amdrColumnData.setAmdrParent(subkeyParentMap.get(stringTriAccumulatorEntry.getKey()));

                return new SimpleEntry<>(
                    stringTriAccumulatorEntry.getKey(), amdrColumnData);

              }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b));

          string.putAll(string1);

          return RowData.builder()
              .childrenNumber(3l)
              .columnDataMap(string)
              .locationIdentifier(mainAccumulatorEntry.getKey())
              .locationName("name")
              .build();

        }).collect(Collectors.toList());

        return collect;

      } else {


        Set<UUID> locSet = amdrDataStream.stream().map(AmdrData::getLocationId)
            .collect(Collectors.toSet());

        List<LocationNameProjection> byIdentifierIn = locationRepository.findLocationNamesByIdentifierIn(
            locSet);

        Map<String, String> locationNameMapById = byIdentifierIn.stream()
            .collect(Collectors.toMap(LocationNameProjection::getIdentifier,
                LocationNameProjection::getLocationName));

        List<AmdrDataLocation> amdrDataLocationList = amdrDataStream.stream()
            .map(amdrData -> AmdrDataLocation
                .builder()
                .data(amdrData.getData())
                .collectionYear(amdrData.getCollectionYear())
                .id(amdrData.getId())
                .datetime(amdrData.getDatetime())
                .locationId(amdrData.getLocationId())
                .overallObject(amdrData.getOverallObject())
                .type(amdrData.getType())
                .overallValue(amdrData.getOverallValue())
                .locationName(locationNameMapById.get(amdrData.getLocationId().toString()))
                .build()
            ).sorted(Comparator.comparing(AmdrDataLocation::getLocationName))
            .collect(Collectors.toList());

        Map<UUID, List<AmdrDataLocation>> collect1 = amdrDataLocationList.stream()
            .collect(Collectors.groupingBy(AmdrDataLocation::getLocationId));

        List<RowData> name = collect1.entrySet().stream().map(amdrLocationEntry -> {

          Map<String, Accumulator> byType =
              amdrLocationEntry.getValue().stream()
                  .collect(Collectors.groupingBy(
                      AmdrDataLocation::getType,
                      Collector.of(
                          Accumulator::new,
                          (acc, d) -> {
                            acc.setNum(
                                acc.getNum()
                                    + d.getOverallObject().getOverallValue()
                            );
                            acc.setDenom(
                                acc.getDenom()
                                    + d.getOverallObject().getOverallTotalRecs()
                            );
                          },
                          (a1, a2) -> {
                            a1.setNum(a1.getNum() + a2.getNum());
                            a1.setDenom(a1.getDenom() + a2.getDenom());
                            return a1;
                          }
                      )
                  ));

          Map<String, ColumnData> string = byType.entrySet().stream().map(entry -> {
            String type = entry.getKey();
            Accumulator value = entry.getValue();
            int val = value.getNum();
            int denom = value.getDenom();
            double perc = denom > 0 ?
                (double) val
                    / (double) denom * 100 :
                (double) 0;
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.00", symbols);
            String format = df.format(perc);
            String meta =
                "total recs: " + denom +
                    " \r\n - obs: " + val + " => "
                    + format + "%";

            AmdrColumnData percentageCol = new AmdrColumnData();

            percentageCol.setValue(format + " " + val);
            percentageCol.setMeta(meta);
            percentageCol.setDataType("string");
            percentageCol.setIsPercentage(true);
            percentageCol.setDescription(amdrHeaderNameMap.get(type));
            percentageCol.setHslColor(amdrHeaderColorMap.get(type));
//            percentageCol.setColTotal(totalsForColumnData.get(amdrLocationEntry.getKey().toString()).getMarkers().get(entry.getKey()));
            return new SimpleEntry<>(type, percentageCol);
          }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b));

          return RowData.builder()
              .childrenNumber(3l)
              .columnDataMap(string)
              .locationIdentifier(amdrLocationEntry.getKey())
              .locationName("name")
              .build();

        }).collect(Collectors.toList());

        return name;
      }
    }
    return null;
  }

  public static <T> List<List<T>> splitList(List<T> originalList, int chunkSize) {
    List<List<T>> chunks = new ArrayList<>();

    for (int i = 0; i < originalList.size(); i += chunkSize) {
      int end = Math.min(originalList.size(), i + chunkSize);
      chunks.add(new ArrayList<>(originalList.subList(i, end)));
    }

    return chunks;
  }

  public AmdrFeatureSetResponse getFeatureSetResponse(UUID parentIdentifier,
      List<PlanLocationDetails> locationDetails, Map<UUID, RowData> rowDataMap,
      String reportLevel, String clickedColumn, Map<String,Map<String, AmdrMarkerStats>> totalsForColumnData) {
    AmdrFeatureSetResponse response = new AmdrFeatureSetResponse();
    response.setType("FeatureCollection");
    List<LocationResponse> locationResponses = locationDetails.stream()
        .map(loc -> LocationResponseFactory.fromPlanLocationDetails(loc, parentIdentifier))
        .collect(Collectors.toList());

    locationResponses = setGeoJsonProperties(rowDataMap, locationResponses);
    response.setDefaultDisplayColumn(clickedColumn);
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    response.setMarkers(totalsForColumnData);


    if (rowDataMap.size() == 0){
      response.setNoDashboardData(true);
    }
    return response;
  }

  public CoordsByYearOrLocationWithTicks get3dData(List<PlanLocationDetails> locationDetails,
      String clickedColumn) {
    List<List<PlanLocationDetails>> lists = splitList(locationDetails
        , 500);

    List<AmdrData> amdrDataList = new ArrayList<>();
    for (List<PlanLocationDetails> planLocationDetails : lists) {

      if (clickedColumn != null) {

        List<AmdrData> amdrData1 = amdrRepository.findByTypeAndLocationIdIn(
            clickedColumn, planLocationDetails.stream()
                .map(locationDetail -> locationDetail.getLocation().getIdentifier())
                .collect(Collectors.toList()));
        amdrDataList.addAll(amdrData1);
      } else {
        List<AmdrData> byLocationIdIn = amdrRepository.findByLocationIdIn(
            planLocationDetails.stream()
                .map(locationDetail -> locationDetail.getLocation().getIdentifier())
                .collect(Collectors.toList()));
        amdrDataList.addAll(byLocationIdIn);

        log.info("{}", byLocationIdIn);
      }

    }

    Set<YearMonth> yearMonths =
        amdrDataList.stream()
            .map(d -> YearMonth.of(
                Integer.parseInt(d.getCollectionYear()),
                Integer.parseInt(d.getCollectionMonth())
            ))
            .collect(Collectors.toSet());

    YearMonth minYearMonth = yearMonths.stream().min(YearMonth::compareTo).get();
    YearMonth maxYearMonth = yearMonths.stream().max(YearMonth::compareTo).get();

    Set<YearMonth> fullRange = new LinkedHashSet<>();

    for (YearMonth ym = minYearMonth; !ym.isAfter(maxYearMonth); ym = ym.plusMonths(1)) {
      fullRange.add(ym);
    }

    List<YearMonth> yearMonthList = new ArrayList<>(fullRange);

    Map<YearMonth, Integer> yearMap = IntStream.range(0, yearMonthList.size())
        .mapToObj(i -> new AbstractMap.SimpleEntry<>(yearMonthList.get(i), i))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));

    Set<String> yearStrings = amdrDataList.stream()
        .map(AmdrData::getCollectionYear)  // returns String
        .collect(Collectors.toCollection(TreeSet::new));  // sorted

    if (!yearStrings.isEmpty()) {
      // convert to ints
      List<Integer> yearsInt = yearStrings.stream()
          .map(Integer::parseInt)
          .sorted()
          .collect(Collectors.toList());

      int min = yearsInt.get(0);
      int max = yearsInt.get(yearsInt.size() - 1);

      // generate full range and convert back to String
      Set<String> fullYears = IntStream.rangeClosed(min, max)
          .mapToObj(String::valueOf)
          .collect(Collectors.toCollection(TreeSet::new));

      yearStrings = fullYears;  // replace original set
    }

    Set<UUID> locationSet = amdrDataList.stream().map(AmdrData::getLocationId)
        .collect(Collectors.toSet());

    List<UUID> locationList = new ArrayList<>(locationSet);

    Map<UUID, Integer> locationMap = IntStream.range(0, locationList.size())
        .mapToObj(i -> new AbstractMap.SimpleEntry<>(locationList.get(i), i))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));

    Set<UUID> uuidStream = locationMap.entrySet().stream()
        .map(Entry::getKey).collect(Collectors.toSet());
    List<LocationNameProjection> byIdentifierIn = locationRepository.findLocationNamesByIdentifierIn(
        uuidStream);
    Map<String, String> locationNameMapById = byIdentifierIn.stream()
        .collect(Collectors.toMap(LocationNameProjection::getIdentifier,
            LocationNameProjection::getLocationName));

    List<AmdrDataLocation> amdrDataLocationList = amdrDataList.stream()
        .map(amdrData -> AmdrDataLocation
            .builder()
            .data(amdrData.getData())
            .collectionYear(amdrData.getCollectionYear())
            .collectionMonth(amdrData.getCollectionMonth())
            .id(amdrData.getId())
            .datetime(amdrData.getDatetime())
            .locationId(amdrData.getLocationId())
            .overallObject(amdrData.getOverallObject())
            .type(amdrData.getType())
            .overallValue(amdrData.getOverallValue())
            .locationName(locationNameMapById.get(amdrData.getLocationId().toString()))
            .build()
        ).collect(Collectors.toList());

    List<AmdrDataLocation> collect = amdrDataLocationList.stream()
        .sorted(Comparator.comparing(AmdrDataLocation::getLocationName))
        .collect(Collectors.toList());

    Map<String, Map<UUID, Coords>> coordsByTypeAndLocation = new HashMap<>();
    Map<String, Map<String, Coords>> coordsByTypeAndYear = new HashMap<>();

    for (AmdrDataLocation amdrData : collect) {

      Map<UUID, Coords> coordsByLocation;
      Map<String, Coords> coordsByYear;
      if (clickedColumn != null) {
        Map<String, Map<String, AmdrCounters>> data = amdrData.getData();

        for (Entry<String, Map<String, AmdrCounters>> counterMap : data.entrySet()) {

          for (Entry<String, AmdrCounters> counterEntry : counterMap.getValue().entrySet()) {

            if (!coordsByTypeAndLocation.containsKey(counterEntry.getKey())) {
              coordsByLocation = new HashMap<>();
            } else {
              coordsByLocation = coordsByTypeAndLocation.get(counterEntry.getKey());
            }

            Coords coords;
            if (!coordsByLocation.containsKey(amdrData.getLocationId())) {
              coords = new Coords();
            } else {
              coords = coordsByLocation.get(amdrData.getLocationId());
            }
            coords.setName(locationNameMapById.get(amdrData.getLocationId().toString()));

            if (!coordsByTypeAndYear.containsKey(counterEntry.getKey())) {
              coordsByYear = new HashMap<>();
            } else {
              coordsByYear = coordsByTypeAndYear.get(counterEntry.getKey());
            }

            Coords yearCoords;
            if (!coordsByYear.containsKey(amdrData.getCollectionYear())) {
              yearCoords = new Coords();
            } else {
              yearCoords = coordsByYear.get(amdrData.getCollectionYear());
            }

            yearCoords.setName(amdrData.getCollectionYear());

            coords.getX().add(yearMap.get(
                YearMonth.of(Integer.parseInt(amdrData.getCollectionYear()),
                    Integer.parseInt(amdrData.getCollectionMonth()))));

            coords.getY().add(locationMap.get(amdrData.getLocationId()));

            yearCoords.getX().add(yearMap.get(
                YearMonth.of(Integer.parseInt(amdrData.getCollectionYear()),
                    Integer.parseInt(amdrData.getCollectionMonth()))));
            yearCoords.getY().add(locationMap.get(amdrData.getLocationId()));

            double mono = counterEntry.getValue().getMono() /
                (counterEntry.getValue().getTotalRecs() != null
                    && counterEntry.getValue().getTotalRecs() > 0 ?
                    counterEntry.getValue().getTotalRecs() : 1) * 100;

            double mixed = counterEntry.getValue().getMixed() /
                (counterEntry.getValue().getTotalRecs() != null
                    && counterEntry.getValue().getTotalRecs() > 0 ?
                    counterEntry.getValue().getTotalRecs() : 1) * 100;

            double total = counterEntry.getValue().getTotal() /
                (counterEntry.getValue().getTotalRecs() != null
                    && counterEntry.getValue().getTotalRecs() > 0 ?
                    counterEntry.getValue().getTotalRecs() : 1) * 100;

            coords.getZ().add(total);
            yearCoords.getZ().add(total);

            coordsByLocation.put(amdrData.getLocationId(), coords);
            coordsByYear.put(amdrData.getCollectionYear(), yearCoords);

            coordsByTypeAndLocation.put(counterEntry.getKey(), coordsByLocation);
            coordsByTypeAndYear.put(counterEntry.getKey(), coordsByYear);
          }
        }

      } else {
        if (!coordsByTypeAndLocation.containsKey(amdrData.getType())) {
          coordsByLocation = new HashMap<>();
        } else {
          coordsByLocation = coordsByTypeAndLocation.get(amdrData.getType());
        }

        Coords coords;
        if (!coordsByLocation.containsKey(amdrData.getLocationId())) {
          coords = new Coords();
        } else {
          coords = coordsByLocation.get(amdrData.getLocationId());
        }
//////////year
        if (!coordsByTypeAndYear.containsKey(amdrData.getType())) {
          coordsByYear = new HashMap<>();
        } else {
          coordsByYear = coordsByTypeAndYear.get(amdrData.getType());
        }

        Coords yearCoords;
        if (!coordsByYear.containsKey(amdrData.getCollectionYear())) {
          yearCoords = new Coords();
        } else {
          yearCoords = coordsByYear.get(amdrData.getCollectionYear());
        }

        coords.getX().add(yearMap.get(
            YearMonth.of(Integer.parseInt(amdrData.getCollectionYear()),
                Integer.parseInt(amdrData.getCollectionMonth()))));

        coords.getY().add(locationMap.get(amdrData.getLocationId()));

        yearCoords.getX().add(yearMap.get(
            YearMonth.of(Integer.parseInt(amdrData.getCollectionYear()),
                Integer.parseInt(amdrData.getCollectionMonth()))));
        yearCoords.getY().add(locationMap.get(amdrData.getLocationId()));

        int val = amdrData.getOverallObject().getOverallValue();
        int denom = amdrData.getOverallObject().getOverallTotalRecs();

        double perc = denom > 0 ?
            (double) val
                / (double) denom * 100 :
            (double) 0;

        coords.getZ().add(perc);
        yearCoords.getZ().add(perc);

        coordsByLocation.put(amdrData.getLocationId(), coords);
        coordsByYear.put(amdrData.getCollectionYear(), yearCoords);

        coordsByTypeAndLocation.put(amdrData.getType(), coordsByLocation);
        coordsByTypeAndYear.put(amdrData.getType(), coordsByYear);
      }
    }

    for (Map<UUID, Coords> coordsByLocation : coordsByTypeAndLocation.values()) {
      for (Coords coords : coordsByLocation.values()) {

        // Create a list of indices
        List<Integer> indices = IntStream.range(0, coords.getX().size())
            .boxed()
            .collect(Collectors.toList());

        // Sort indices by corresponding x value
        indices.sort(Comparator.comparing(coords.getX()::get));

        // Reorder all lists based on sorted indices
        List<Integer> xSorted = new ArrayList<>();
        List<Integer> ySorted = new ArrayList<>();
        List<Double> zSorted = new ArrayList<>();

        for (int i : indices) {
          xSorted.add(coords.getX().get(i));
          ySorted.add(coords.getY().get(i));
          zSorted.add(coords.getZ().get(i));
        }

        // Replace the original lists
        coords.setX(xSorted);
        coords.setY(ySorted);
        coords.setZ(zSorted);
      }
    }

    for (Map<String, Coords> coordsByYear : coordsByTypeAndYear.values()) {
      for (Coords coords : coordsByYear.values()) {

        // Create a list of indices
        List<Integer> indices = IntStream.range(0, coords.getY().size())
            .boxed()
            .collect(Collectors.toList());

        // Sort indices by corresponding x value
        indices.sort(Comparator.comparing(coords.getY()::get));

        // Reorder all lists based on sorted indices
        List<Integer> xSorted = new ArrayList<>();
        List<Integer> ySorted = new ArrayList<>();
        List<Double> zSorted = new ArrayList<>();

        for (int i : indices) {
          xSorted.add(coords.getX().get(i));
          ySorted.add(coords.getY().get(i));
          zSorted.add(coords.getZ().get(i));
        }

        // Replace the original lists
        coords.setX(xSorted);
        coords.setY(ySorted);
        coords.setZ(zSorted);
      }
    }

    List<String> yearNames = new ArrayList<>();
    List<Integer> yearValues = new ArrayList<>();

    yearMap.forEach((key, value) -> {
      if (!yearNames.contains(key)) {
        yearNames.add(key.getMonth().equals(Month.JANUARY) ?
            key.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).concat(" ")
                .concat(String.valueOf(key.getYear()))
            :
                key.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)));
        yearValues.add(value);
      }
    });

    Set<Integer> yearDimTickLocationValues = locationMap.entrySet().stream()
        .map(Entry::getValue)
        .collect(Collectors.toSet());

    Set<String> yLocationNames = byIdentifierIn.stream()
        .map(LocationNameProjection::getLocationName)
        .collect(Collectors.toSet());

    CoordsByYearOrLocation coords = new CoordsByYearOrLocation();
    coords.setCoordsByTypeAndLocation(coordsByTypeAndLocation);
    coords.setCoordsByTypeAndYear(coordsByTypeAndYear);

    CoordsByYearOrLocationWithTicks coordsByYearOrLocationWithTicks = new CoordsByYearOrLocationWithTicks();
    coordsByYearOrLocationWithTicks.setCoordsByYearOrLocation(coords);
    coordsByYearOrLocationWithTicks.setXTickNames(yearNames);
    coordsByYearOrLocationWithTicks.setXTickValues(yearValues);
    coordsByYearOrLocationWithTicks.setYTickNames(yLocationNames);
    coordsByYearOrLocationWithTicks.setYTickValues(yearDimTickLocationValues);

    log.info("{}", coordsByTypeAndLocation);
    return coordsByYearOrLocationWithTicks;

  }


  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CoordsByYearOrLocation implements Serializable {

    Map<String, Map<UUID, Coords>> coordsByTypeAndLocation = new HashMap<>();
    Map<String, Map<String, Coords>> coordsByTypeAndYear = new HashMap<>();

  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CoordsByYearOrLocationWithTicks implements Serializable {

    CoordsByYearOrLocation coordsByYearOrLocation;

    List<Integer> xTickValues = new ArrayList<>();
    List<String> xTickNames = new ArrayList<>();

    Set<Integer> yTickValues = new HashSet<>();
    Set<String> yTickNames = new HashSet<>();
  }


  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Coords implements Serializable {

    private List<Integer> x = new ArrayList<>();
    private List<Integer> y = new ArrayList<>();
    private List<Double> z = new ArrayList<>();
    private String name;

  }

  private List<LocationResponse> setGeoJsonProperties(Map<UUID, RowData> rowDataMap,
      List<LocationResponse> locationResponses) {
    return locationResponses.stream()
        .filter(loc -> rowDataMap.containsKey(loc.getIdentifier()))
        .peek(loc -> {
          loc.getProperties()
              .setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
          loc.getProperties().setId(loc.getIdentifier().toString());

        }).collect(Collectors.toList());
  }

}
