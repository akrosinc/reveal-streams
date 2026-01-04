package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trace implements Serializable {

  private String type;               // e.g., "bar"
  private List<String> x = new ArrayList<>();            // x-axis values (dates)
  private List<Integer> y = new ArrayList<>();           // y values
  private List<List<Integer>> customdata = new ArrayList<>();  // custom data for hover
  private List<Integer> additionalData = new ArrayList<>();  // custom data for hover
      // base values
  private String name;               // trace name
  private Integer width;             // width in milliseconds
  private Marker marker;             // marker properties
  private String hovertemplate;      // hover template



}

