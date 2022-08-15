package com.revealprecision.revealstreams.messaging.message;

import com.revealprecision.revealstreams.persistence.domain.metadata.infra.MetadataObj;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MetadataObjEvent extends Message {

  private UUID entityId;

  private UUID locationHierarchy;

  private String locationGeographicLevel;

  private MetadataObj metadataObj;

  private UUID ancestor;

}
