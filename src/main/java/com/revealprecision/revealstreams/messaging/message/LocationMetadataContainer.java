package com.revealprecision.revealstreams.messaging.message;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationMetadataContainer extends Message{

  private UUID locationIdentifier;

  private MetaDataEvent metaDataEvent;

  private List<UUID> ancestry;

}
