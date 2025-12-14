package com.revealprecision.revealstreams.models.amdr;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmdrDataLocation {

  private UUID id;

  private UUID locationId;

  private String type;

  private Double overallValue;

  private Map<String,Map<String, AmdrCounters>> data;

  private AmdrOverall overallObject;

  private LocalDateTime datetime;

  private String collectionYear;

  private String collectionMonth;

  private String locationName;

}
