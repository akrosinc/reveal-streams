package com.revealprecision.revealstreams.models;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeoLevelItem implements Serializable {

  UUID identifier;
  String name;
}
