package com.revealprecision.revealstreams.models;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationNodeDetails implements Serializable {

  List<String> geoLevels;
  List<LocationNode> nodes;
}
