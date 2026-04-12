package com.revealprecision.revealstreams.models.amdr;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AmdrPerformanceAggregatedDataString implements Serializable {
 private String parentIdentifier;
 private String parentName;
 private String locationIdentifier;
 private String locationName;
 private Map<String,String> stateCounts;
}
