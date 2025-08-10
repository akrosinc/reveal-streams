package com.revealprecision.revealstreams.persistence.projection;


import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class IndividualsPerCompoundByLocationProjectionObj implements Serializable {

  String compound;

  String locationIdentifier;

  int individualCount;
}
