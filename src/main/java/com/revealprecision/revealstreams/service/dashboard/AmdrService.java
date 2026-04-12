package com.revealprecision.revealstreams.service.dashboard;

import static com.revealprecision.revealstreams.service.dashboard.DashboardService.ALL_OTHER_LEVELS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revealprecision.revealstreams.dto.AmdrFeatureSetResponse;
import com.revealprecision.revealstreams.dto.LocationPropertyResponse;
import com.revealprecision.revealstreams.dto.LocationResponse;
import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.dto.amdr.AmdrLandPageResponse;
import com.revealprecision.revealstreams.enums.amdr.AmdrColumnType;
import com.revealprecision.revealstreams.enums.amdr.AmdrDateModes;
import com.revealprecision.revealstreams.factory.LocationResponseFactory;
import com.revealprecision.revealstreams.models.ColumnData;
import com.revealprecision.revealstreams.models.LocationNode;
import com.revealprecision.revealstreams.models.LocationNodeDetails;
import com.revealprecision.revealstreams.models.RowData;
import com.revealprecision.revealstreams.models.amdr.AmdrChartData;
import com.revealprecision.revealstreams.models.amdr.AmdrColumnData;
import com.revealprecision.revealstreams.models.amdr.AmdrCounters;
import com.revealprecision.revealstreams.models.amdr.AmdrDataLocation;
import com.revealprecision.revealstreams.models.amdr.AmdrDrugMarkerStats;
import com.revealprecision.revealstreams.models.amdr.AmdrDrugYearlyMonthlyDate;
import com.revealprecision.revealstreams.models.amdr.AmdrDrugYearlyMonthlyLocational;
import com.revealprecision.revealstreams.models.amdr.AmdrDrugYearlyMonthlyLocationalItem;
import com.revealprecision.revealstreams.models.amdr.AmdrHaplotypeYearlyMonthlyDate;
import com.revealprecision.revealstreams.models.amdr.AmdrHaplotypeYearlyMonthlyLocational;
import com.revealprecision.revealstreams.models.amdr.AmdrHaplotypeYearlyMonthlyLocationalItem;
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
import com.revealprecision.revealstreams.persistence.domain.amdr.HslColor;
import com.revealprecision.revealstreams.persistence.projection.LocationNameProjection;
import com.revealprecision.revealstreams.persistence.projection.amdr.AmdrDataCalcMatProjection;
import com.revealprecision.revealstreams.persistence.projection.amdr.AmdrYearlyMonthlyLocationProjection;
import com.revealprecision.revealstreams.persistence.projection.amdr.LandingPageCountsProjection;
import com.revealprecision.revealstreams.persistence.repository.LocationHierarchyRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationRelationshipRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationRepository;
import com.revealprecision.revealstreams.persistence.repository.amdr.AmdrDataCalMatRepository;
import com.revealprecision.revealstreams.persistence.repository.amdr.AmdrHeaderNamesRepository;
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

//  private final AmdrMappingsRepository amdrMappingsRepository;

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
  public static class HeaderName implements Serializable {

    private HslColor color;
    private String name;
    private int order;


  }

  public Map<AmdrColumnType, Map<String, HeaderName>> getHeadersForReport() {
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
                    .order(item.getColOrder())
                    .build()
            )
        ));

    Map<String, HeaderName> haplotypeMap = amdrHeaderNamesRepository.findAllByColType(
            AmdrColumnType.DRUG.name())
        .stream()
        .filter(item -> !List.of("dhfr_dhps", "crt_mdr1", "dhfr").contains(item.getId().getKey()))
        .collect(// outer key
            Collectors.toMap(
                item -> item.getId().getKey(), // inner map key
                item -> HeaderName.builder()   // inner map value
                    .color(item.getColor())
                    .name(item.getId().getKey())
                    .order(item.getColOrder())
                    .build()
            )
        );
    Map<AmdrColumnType, Map<String, HeaderName>> haplotypeList =
        Map.of(AmdrColumnType.HAPLOTYPE, haplotypeMap);

    Map<String, HeaderName> geneMap = amdrHeaderNamesRepository.findAllByColType(
            AmdrColumnType.HAPLOTYPE.name())
        .stream()
        .collect(// outer key
            Collectors.toMap(
                item -> item.getId().getKey(), // inner map key
                item -> HeaderName.builder()   // inner map value
                    .color(item.getColor())
                    .name(item.getId().getKey())
                    .order(item.getColOrder())
                    .build()
            )
        );


    Map<AmdrColumnType, Map<String, HeaderName>> genetypeList =
        Map.of(AmdrColumnType.GENE, geneMap);


    Map<AmdrColumnType, Map<String, HeaderName>> combinedMap = new HashMap<>();
    combinedMap.putAll(drugList);
    combinedMap.putAll(haplotypeList);
    combinedMap.putAll(genetypeList);

    return combinedMap;

  }

  public LocationNodeDetails getLocationTree() {

    LocationHierarchy aDefault = locationHierarchyRepository.findLocationHierarchyByName("default");
    List<LocationNode> locationNodes = locationService.buildTree();
    return new LocationNodeDetails(aDefault.getNodeOrder().stream().filter(item->!"structure".equals(item)).collect(
        Collectors.toList()),locationNodes );

  }

  public AmdrFeatureSetResponse getDataForReportForDateForHaplotype(
      String parentIdentifierString, AmdrColumnType dashboardView, List<UUID> locationList,
      AmdrDateModes amdrDateModes) {

    List<AmdrHeaderNames> amdrHeaderNames = amdrHeaderNamesRepository.findAll();

    Map<String, String> amdrHeaderNameMap = amdrHeaderNames.stream()
        .collect(
            Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getName, (a, b) -> b));


    Map<String, HslColor> amdrHeaderColorMap = amdrHeaderNames.stream()
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getColor,
            (a, b) -> b));


    List<UUID> locIds = new ArrayList<>();
    if (locationList.isEmpty()) {
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
      locIds.add(collect.get(0).getLocation().getIdentifier());
    } else {
      locIds.addAll(locationList);
    }
    List<LocationPropertyResponse> rowData = new ArrayList<>();

    if (AmdrDateModes.YEARLY.equals(amdrDateModes)){
      List<AmdrYearlyMonthlyLocationProjection> yearlyAggregation = amdrRepository.getYearlyAggregationForHaplotype(
          locIds);

      List<AmdrHaplotypeYearlyMonthlyDate> collect1 = yearlyAggregation.stream().map(yearlRecord -> {

        Map<String,Map<String, AmdrDrugMarkerStats>> markersMap;
        try {
          markersMap = objectMapper.readValue(
              yearlRecord.getData(),
              new TypeReference<>() {
              });
        } catch (Exception e) {
          markersMap = null;
        }
        return new AmdrHaplotypeYearlyMonthlyDate(yearlRecord.getCollectionYear(), markersMap);

      }).collect(Collectors.toList());

      rowData = getRowsForDateHaplotype(collect1,amdrHeaderColorMap,amdrHeaderNameMap);
    } else {
      List<AmdrYearlyMonthlyLocationProjection> yearlyAggregation = amdrRepository.getYearlyMonthlyAggregationForHaplotype(
          locIds);

      List<AmdrHaplotypeYearlyMonthlyDate> collect1 = yearlyAggregation.stream().map(yearlyRecord -> {

        Map<String,Map<String, AmdrDrugMarkerStats>> markersMap;
        try {
          markersMap = objectMapper.readValue(
              yearlyRecord.getData(),
              new TypeReference<>() {
              });
        } catch (Exception e) {
          markersMap = null;
        }

        YearMonth yearMonth = YearMonth.of((int) Double.parseDouble(yearlyRecord.getCollectionYear())
            , (int) Double.parseDouble(yearlyRecord.getCollectionMonth()));

        String yyyy_mmm = yearMonth.format(DateTimeFormatter.ofPattern("yyyy MMM"));
        return new AmdrHaplotypeYearlyMonthlyDate(yyyy_mmm, markersMap);

      }).collect(Collectors.toList());

      rowData = getRowsForDateHaplotype(collect1,amdrHeaderColorMap,amdrHeaderNameMap);
    }
    List<Location> byListOfIdentifiers = locationService.findByListOfIdentifiers(locIds);

    return getFeatureSetResponseDate(byListOfIdentifiers,
        rowData, ALL_OTHER_LEVELS, dashboardView, null);
  }

  public  Map<String, HslColor>  getColorMap(){

    List<AmdrHeaderNames> amdrHeaderNames = amdrHeaderNamesRepository.findAll();

    Map<String, HslColor> amdrHeaderColorMap = amdrHeaderNames.stream()
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getColor,
            (a, b) -> b));

    return amdrHeaderColorMap;
  }

  public List<AmdrHaplotypeYearlyMonthlyLocational>  getDataForReportForDateForHaplotypeLocation(
      String parentIdentifierString,  List<UUID> locationList,
      AmdrDateModes amdrDateModes) {

    List<UUID> locIds = new ArrayList<>();
    if (locationList.isEmpty()) {
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
        return new ArrayList<>();
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
      locIds.add(collect.get(0).getLocation().getIdentifier());
    } else {
      locIds.addAll(locationList);
    }

    List<AmdrHaplotypeYearlyMonthlyLocational> locationalData = new ArrayList<>();

    List<AmdrYearlyMonthlyLocationProjection> amdrYearlyMonthlyLocationProjections;

    if (AmdrDateModes.YEARLY.equals(amdrDateModes)){

      amdrYearlyMonthlyLocationProjections = amdrRepository.getYearlyLocationalAggregationForHaplotype(
          locIds);

    } else {

      amdrYearlyMonthlyLocationProjections = amdrRepository.getYearlyMonthlyLocationalAggregationForHaplotype(
          locIds);

    }

    locationalData = amdrYearlyMonthlyLocationProjections.stream()
        .map(yearlyRecord -> {
          List<AmdrHaplotypeYearlyMonthlyLocationalItem> markersList;
          try {
            markersList = objectMapper.readValue(
                yearlyRecord.getData(),
                new TypeReference<>() {
                });
          } catch (Exception e) {
            markersList = null;
          }
          return new AmdrHaplotypeYearlyMonthlyLocational(
              yearlyRecord.getLocationIdentifier().toString(), markersList);
        }).collect(Collectors.toList());

    return locationalData;

  }

  public AmdrFeatureSetResponse getDataForReportForDateForDrug(
      String parentIdentifierString, AmdrColumnType dashboardView, List<UUID> locationList,
       AmdrDateModes amdrDateModes) {

    List<AmdrHeaderNames> amdrHeaderNames = amdrHeaderNamesRepository.findAll();

    Map<String, String> amdrHeaderNameMap = amdrHeaderNames.stream()
        .collect(
            Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getName, (a, b) -> b));


    Map<String, HslColor> amdrHeaderColorMap = amdrHeaderNames.stream()
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getColor,
            (a, b) -> b));

    Map<String, List<String>> amdrHeaderContributingColumnsMap = amdrHeaderNames.stream()
        .filter(item -> item.getContributingCol()!= null && !item.getContributingCol().isEmpty())
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getContributingCol,
            (a, b) -> b));


    List<UUID> locIds = new ArrayList<>();
    if (locationList.isEmpty()) {
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
      locIds.add(collect.get(0).getLocation().getIdentifier());
    } else {
      locIds.addAll(locationList);
    }
    List<LocationPropertyResponse> rowData = new ArrayList<>();

    if (AmdrDateModes.YEARLY.equals(amdrDateModes)){
      List<AmdrYearlyMonthlyLocationProjection> yearlyAggregation = amdrRepository.getYearlyAggregationForDrug(
          locIds);

      List<AmdrDrugYearlyMonthlyDate> collect1 = yearlyAggregation.stream().map(yearlRecord -> {

        Map<String, AmdrDrugMarkerStats> markersMap;
        try {
          markersMap = objectMapper.readValue(
              yearlRecord.getData(),
              new TypeReference<>() {
              });
        } catch (Exception e) {
          markersMap = null;
        }
        return new AmdrDrugYearlyMonthlyDate(yearlRecord.getCollectionYear(), markersMap);

      }).collect(Collectors.toList());



       rowData = getRowsforDateDrug(collect1,amdrHeaderColorMap,amdrHeaderNameMap,amdrHeaderContributingColumnsMap);
    } else {
      List<AmdrYearlyMonthlyLocationProjection> yearlyAggregation = amdrRepository.getYearlyMonthlyAggregationForDrug(
          locIds);

      List<AmdrDrugYearlyMonthlyDate> collect1 = yearlyAggregation.stream().map(yearlRecord -> {

        Map<String, AmdrDrugMarkerStats> markersMap;
        try {
          markersMap = objectMapper.readValue(
              yearlRecord.getData(),
              new TypeReference<>() {
              });
        } catch (Exception e) {
          markersMap = null;
        }

        YearMonth yearMonth = YearMonth.of((int) Double.parseDouble(yearlRecord.getCollectionYear())
            , (int) Double.parseDouble(yearlRecord.getCollectionMonth()));

        String yyyy_mmm = yearMonth.format(DateTimeFormatter.ofPattern("yyyy MMM"));
        return new AmdrDrugYearlyMonthlyDate(yyyy_mmm, markersMap);

      }).collect(Collectors.toList());


      rowData = getRowsforDateDrug(collect1,amdrHeaderColorMap,amdrHeaderNameMap,amdrHeaderContributingColumnsMap);


    }


    List<Location> byListOfIdentifiers = locationService.findByListOfIdentifiers(locIds);

    return getFeatureSetResponseDate(byListOfIdentifiers,
        rowData, ALL_OTHER_LEVELS, dashboardView, null);

  }

  public  List<AmdrDrugYearlyMonthlyLocational> getDataForReportForDateForDrugLocation(
      String parentIdentifierString, List<UUID> locationList,
      AmdrDateModes amdrDateModes) {

    List<AmdrHeaderNames> amdrHeaderNames = amdrHeaderNamesRepository.findAll();

    Map<String, HslColor> amdrHeaderColorMap = amdrHeaderNames.stream()
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getColor,
            (a, b) -> b));

    List<UUID> locIds = new ArrayList<>();
    if (locationList.isEmpty()) {
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
        return new ArrayList<>();

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
      locIds.add(collect.get(0).getLocation().getIdentifier());
    } else {
      locIds.addAll(locationList);
    }

    List<AmdrDrugYearlyMonthlyLocational> locationalData = new ArrayList<>();

    List<AmdrYearlyMonthlyLocationProjection> yearlyMonthlyLocationalAggregationForDrug;

    if (AmdrDateModes.YEARLY.equals(amdrDateModes)){

      yearlyMonthlyLocationalAggregationForDrug = amdrRepository.getYearlyLocationalAggregationForDrug(
          locIds);

    } else {

      yearlyMonthlyLocationalAggregationForDrug = amdrRepository.getYearlyMonthlyLocationalAggregationForDrug(
          locIds);

    }

    locationalData = yearlyMonthlyLocationalAggregationForDrug.stream()
        .map(yearlyRecord -> {
          List<AmdrDrugYearlyMonthlyLocationalItem> markersList;
          try {
            markersList = objectMapper.readValue(
                yearlyRecord.getData(),
                new TypeReference<>() {
                });
          } catch (Exception e) {
            markersList = null;
          }
          return new AmdrDrugYearlyMonthlyLocational(
              yearlyRecord.getLocationIdentifier().toString(), markersList);
        }).collect(Collectors.toList());

    return locationalData;

  }

  public AmdrFeatureSetResponse getDataForReportForGeography(
      String parentIdentifierString, AmdrColumnType dashboardView) {

    List<AmdrHeaderNames> amdrHeaderNames = amdrHeaderNamesRepository.findAll();

    Map<String, String> amdrHeaderNameMap = amdrHeaderNames.stream()
        .collect(
            Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getName, (a, b) -> b));

    Map<String, HslColor> amdrHeaderColorMap = amdrHeaderNames.stream()
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getColor,
            (a, b) -> b));

    Map<String, List<String>> amdrHeaderContributingColumnsMap = amdrHeaderNames.stream()
        .filter(item -> item.getContributingCol()!= null && !item.getContributingCol().isEmpty())
        .collect(Collectors.toMap(item -> item.getId().getKey(), AmdrHeaderNames::getContributingCol,
            (a, b) -> b));

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

    List<RowData> rowData = getRows3(parentLocation, collect, dashboardView, amdrHeaderNameMap,
        amdrHeaderColorMap, maxYear, lists,amdrHeaderContributingColumnsMap);

    List<Location> parent;
    if (parentIdentifier == null) {
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
    Map<String, Map<String, AmdrMarkerStats>> markersMap;
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
        rowDataMap, ALL_OTHER_LEVELS, dashboardView, markersMap);
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

  public List<LocationPropertyResponse> getRowsforDateDrug(List<AmdrDrugYearlyMonthlyDate> yearlyAggregation
      , Map<String, HslColor> amdrHeaderColorMap, Map<String, String> amdrHeaderNameMap
      , Map<String, List<String>> amdrHeaderContributingColumnsMap) {

    List<LocationPropertyResponse> collect1 = yearlyAggregation.stream().map(yearRecord -> {
      Map<String, ColumnData> collect = yearRecord.getData().entrySet().stream()
          .map(yearEntry -> {
            String type = yearEntry.getKey();
            AmdrDrugMarkerStats value = yearEntry.getValue();
            Long val = value.getVal();
            Long denom = value.getRec();
            double perc = denom > 0 ?
                (double) val
                    / (double) denom * 100 :
                (double) 0;

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.00", symbols);
            String format = df.format(perc);
//            String meta =
//                "total recs: " + denom +
//                    " \r\n - obs: " + val + " => "
//                    + format + "%";
            String collect2 = amdrHeaderContributingColumnsMap.get(type).stream()
                .map(item -> amdrHeaderNameMap.get(item))
                .collect(Collectors.joining(" \r\n"));
            String meta =
                collect2 +
                "obs: " + val + " / total recs: " + denom  + " => "
                    + format + "%";

            AmdrColumnData percentageCol = new AmdrColumnData();

            percentageCol.setValue(format + " " + val);
            percentageCol.setMeta(meta);
            percentageCol.setDataType("string");
            percentageCol.setIsPercentage(true);
            percentageCol.setDescription(amdrHeaderNameMap.get(type));
            return new SimpleEntry<>(type, percentageCol);
          }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b));

      return LocationPropertyResponse.builder()
          .columnDataMap(collect)
          .name(yearRecord.getName())
          .build();

    }).collect(Collectors.toList());

    return collect1;
  }


  public List<LocationPropertyResponse> getRowsForDateHaplotype(List<AmdrHaplotypeYearlyMonthlyDate> yearlyAggregation
      , Map<String, HslColor> amdrHeaderColorMap, Map<String, String> amdrHeaderNameMap) {

    List<LocationPropertyResponse> string = yearlyAggregation.stream().map(yearRecordItem -> {

      Map<String, ColumnData> result =
          yearRecordItem.getData().entrySet().stream().flatMap(yearRecord ->
                  yearRecord.getValue().entrySet().stream().map(yearEntry -> {
                        String type = yearEntry.getKey();
                        AmdrDrugMarkerStats value = yearEntry.getValue();

                        Long val = value.getVal();
                        Long denom = value.getRec();

                        double perc = denom > 0
                            ? (double) val / (double) denom * 100
                            : 0;

                        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                        symbols.setDecimalSeparator('.');
                        DecimalFormat df = new DecimalFormat("0.00", symbols);
                        String format = df.format(perc);

                        String meta =
                            "obs: " + val + " / total recs: " + denom  + " => "
                                + format + "%";

                        AmdrColumnData percentageCol = new AmdrColumnData();
                        percentageCol.setValue(format + " " + val);
                        percentageCol.setMeta(meta);
                        percentageCol.setDataType("string");
                        percentageCol.setIsPercentage(true);
                        percentageCol.setDescription(amdrHeaderNameMap.get(type));
                        percentageCol.setAmdrParent(yearRecord.getKey());
                        return new AbstractMap.SimpleEntry<>(type, percentageCol);
                      })
              )
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a ));

      return LocationPropertyResponse.builder()
          .columnDataMap(result)
          .name(yearRecordItem.getName())
          .build();

    }).collect(Collectors.toList());


    return string;
  }

  public List<RowData> getRows3(
      Location parentLocation, List<PlanLocationDetails> locationDetails,
      AmdrColumnType dashboardView,
      Map<String, String> amdrHeaderNameMap, Map<String, HslColor> amdrHeaderColorMap, Long maxYear,
      List<List<PlanLocationDetails>> lists, Map<String, List<String>> amdrHeaderContributingColumnsMap) {

    List<AmdrData> amdrDataStream = lists.stream()
        .flatMap(listOfLocationIds ->
            amdrRepository.findByLocationIdInAndCollectionYearIn(
                    listOfLocationIds.stream()
                        .map(locationDetail -> locationDetail.getLocation().getIdentifier())
                        .collect(Collectors.toList()),
                    List.of(String.valueOf(maxYear), "2025"))
                .stream()
        ).collect(Collectors.toList());


    // -------------------------------
    // 2. Location names
    // -------------------------------
    Set<UUID> locationIds = amdrDataStream.stream()
        .map(AmdrData::getLocationId)
        .collect(Collectors.toSet());

    Map<String, String> locationNameMap = locationRepository
        .findLocationNamesByIdentifierIn(locationIds)
        .stream()
        .collect(Collectors.toMap(
            LocationNameProjection::getIdentifier,
            LocationNameProjection::getLocationName
        ));

    // -------------------------------
    // 3. Transform + group
    // -------------------------------
    Map<UUID, List<AmdrDataLocation>> byLocation =
        amdrDataStream.stream()
            .map(data -> AmdrDataLocation.builder()
                .id(data.getId())
                .data(data.getData())
                .collectionYear(data.getCollectionYear())
                .datetime(data.getDatetime())
                .locationId(data.getLocationId())
                .overallObject(data.getOverallObject())
                .type(data.getType())
                .overallValue(data.getOverallValue())
                .locationName(locationNameMap.get(data.getLocationId().toString()))
                .build()
            )
            .sorted(Comparator.comparing(AmdrDataLocation::getLocationName))
            .collect(Collectors.groupingBy(AmdrDataLocation::getLocationId));

    if (maxYear != null) {
      if (AmdrColumnType.HAPLOTYPE.equals(dashboardView)) {


        // -------------------------------
        // 1. Subkey → parent map
        // -------------------------------
        Map<String, String> subkeyParentMap = amdrHeaderNamesRepository
            .findAllByColType(AmdrColumnType.HAPLOTYPE.name())
            .stream()
            .filter(item -> !List.of("dhfr_dhps", "crt_mdr1", "dhfr")
                .contains(item.getId().getKey()))
            .collect(Collectors.toMap(
                item -> item.getId().getKey(),
                AmdrHeaderNames::getColParent
            ));


        // -------------------------------
        // 4. TRI ACCUMULATOR ONLY
        // -------------------------------
        Map<UUID, Map<String, TriAccumulator>> triAccumulator = new HashMap<>();

        for (var entry : byLocation.entrySet()) {

          UUID locationId = entry.getKey();
          List<AmdrDataLocation> locList = entry.getValue();

          for (AmdrDataLocation loc : locList) {
            for (var outer : loc.getData().entrySet()) {
              for (var inner : outer.getValue().entrySet()) {

                String subKey = inner.getKey();
                AmdrCounters counters = inner.getValue();

                TriAccumulator tri = triAccumulator
                    .computeIfAbsent(locationId, k -> new HashMap<>())
                    .computeIfAbsent(subKey, k -> new TriAccumulator());

                tri.setMono(tri.getMono() + counters.getMono());
                tri.setMixed(tri.getMixed() + counters.getMixed());
                tri.setTotal(tri.getTotal() + counters.getTotal());
                tri.setTotalRecs(tri.getTotalRecs() + counters.getTotalRecs());
              }
            }
          }
        }

        // -------------------------------
        // 5. Formatter
        // -------------------------------
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        // -------------------------------
        // 6. Build response (NOW FROM TRI ONLY)
        // -------------------------------
        List<RowData> string = triAccumulator.entrySet().stream()
            .map(entry -> {

              UUID locationId = entry.getKey();
              Map<String, TriAccumulator> triMap = entry.getValue();

              Map<String, ColumnData> columnData = triMap.entrySet().stream()
                  .map(e -> {

                    String subKey = e.getKey();
                    TriAccumulator v = e.getValue();

                    double totalRecs = (v.getTotalRecs() != null && v.getTotalRecs() > 0)
                        ? v.getTotalRecs()
                        : 1;

                    double mono = v.getMono() / totalRecs * 100;
                    double mixed = v.getMixed() / totalRecs * 100;
                    double total = v.getTotal() / totalRecs * 100;

                    String totalVal = df.format(total);

                    String meta =
                        "total recs: " + v.getTotalRecs() +
                            "\n - mono: " + v.getMono() + " => " + df.format(mono) + "%" +
                            "\n - mixed: " + v.getMixed() + " => " + df.format(mixed) + "%" +
                            "\n - total: " + v.getTotal() + " => " + totalVal + "%";

                    AmdrColumnData col = new AmdrColumnData();
                    col.setValue(totalVal);
                    col.setMeta(meta);
                    col.setDataType("string");
                    col.setIsPercentage(true);
                    col.setDescription(amdrHeaderNameMap.get(subKey));
                    col.setAmdrParent(subkeyParentMap.get(subKey));

                    return new SimpleEntry<>(subKey, col);
                  })
                  .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

              return RowData.builder()
                  .childrenNumber(3L)
                  .columnDataMap(columnData)
                  .locationIdentifier(locationId)
                  .locationName(locationNameMap.get(locationId.toString()))
                  .build();
            })
            .collect(Collectors.toList());
        return string;

      } else {

        List<RowData> name = byLocation.entrySet().stream().map(amdrLocationEntry -> {

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
//            String meta =
//                "total recs: " + denom +
//                    " \r\n - obs: " + val + " => "
//                    + format + "%";
            String collect2 = amdrHeaderContributingColumnsMap.get(type).stream()
                .map(item -> amdrHeaderNameMap.get(item))
                .collect(Collectors.joining("\r\n"));

            String meta =
                collect2 + "\r\n" +
                "obs: " + val + " / total recs: " + denom  + " => "
                    + format + "%";

            AmdrColumnData percentageCol = new AmdrColumnData();

            percentageCol.setValue(format + " " + val);
            percentageCol.setMeta(meta);
            percentageCol.setDataType("string");
            percentageCol.setIsPercentage(true);
            percentageCol.setDescription(amdrHeaderNameMap.get(type));
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

  public AmdrFeatureSetResponse getFeatureSetResponseDate(
      List<Location> locationDetails, List<LocationPropertyResponse> rowDataMap,
      String reportLevel, AmdrColumnType dashboardView,
      Map<String, Map<String, AmdrMarkerStats>> totalsForColumnData) {
    AmdrFeatureSetResponse response = new AmdrFeatureSetResponse();
    response.setType("FeatureCollection");
    List<LocationResponse> locationResponses = locationDetails.stream()
        .map(LocationResponseFactory::fromPlanLocationDetailsForDate)
        .collect(Collectors.toList());
    response.setDefaultDisplayColumn(dashboardView.name());
    response.setFeatures(locationResponses);
//    response.setIdentifier("Date");
    response.setMarkers(totalsForColumnData);
    response.setRows(rowDataMap);

    if (rowDataMap.size() == 0) {
      response.setNoDashboardData(true);
    }
    return response;
  }

  public AmdrFeatureSetResponse getFeatureSetResponse(UUID parentIdentifier,
      List<PlanLocationDetails> locationDetails, Map<UUID, RowData> rowDataMap,
      String reportLevel, AmdrColumnType dashboardView,
      Map<String, Map<String, AmdrMarkerStats>> totalsForColumnData) {
    AmdrFeatureSetResponse response = new AmdrFeatureSetResponse();
    response.setType("FeatureCollection");
    List<LocationResponse> locationResponses = locationDetails.stream()
        .map(loc -> LocationResponseFactory.fromPlanLocationDetails(loc, parentIdentifier))
        .collect(Collectors.toList());

    locationResponses = setGeoJsonProperties(rowDataMap, locationResponses);
    response.setDefaultDisplayColumn(dashboardView.name());
    response.setFeatures(locationResponses);
    response.setIdentifier(parentIdentifier);
    response.setMarkers(totalsForColumnData);

    if (rowDataMap.size() == 0) {
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
//  private List<LocationResponse> setGeoJsonPropertiesDate( RowData rowDataMap,
//      List<LocationResponse> locationResponses) {
//    return locationResponses.stream()
//        .filter(loc -> rowDataMap.containsKey(loc.getIdentifier()))
//        .peek(loc -> {
//          loc.getProperties()
//              .setColumnDataMap(rowDataMap.get(loc.getIdentifier()).getColumnDataMap());
//          loc.getProperties().setId(loc.getIdentifier().toString());
//
//        }).collect(Collectors.toList());
//  }

}
