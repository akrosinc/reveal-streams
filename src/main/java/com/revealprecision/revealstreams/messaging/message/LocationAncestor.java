package com.revealprecision.revealstreams.messaging.message;

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
public class LocationAncestor extends Message{

  private String geoName;

  private String locationId;

  private Integer nodePosition;

}
