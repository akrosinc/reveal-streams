package com.revealprecision.revealstreams.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class LocationMetadataEvent extends TMetadataEvent {
  private String planTargetType;
  private String entityGeographicLevel;
  private String locationName;


}
