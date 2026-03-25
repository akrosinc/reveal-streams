package com.revealprecision.revealstreams.persistence.domain.amdr;


import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AmdrDataCalcMatId implements Serializable {

  private String ancestor;
  private String locationname;
  private String geoname;
  private String collectionYear;
  private String collectionMonth;
}
