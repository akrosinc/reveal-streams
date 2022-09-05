package com.revealprecision.revealstreams.persistence.es;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.revealprecision.revealstreams.persistence.domain.Geometry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.GeoShapeField;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "location")
public class LocationValidationElastic {

  @GeoShapeField
  Geometry geometry;
}
