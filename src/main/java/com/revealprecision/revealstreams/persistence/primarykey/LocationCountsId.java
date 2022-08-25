package com.revealprecision.revealstreams.persistence.primarykey;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class LocationCountsId implements Serializable {

  private UUID locationHierarchyIdentifier;

  private UUID parentLocationIdentifier;

  private String geographicLevelName;
}
