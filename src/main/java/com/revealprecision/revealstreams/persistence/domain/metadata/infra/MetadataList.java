package com.revealprecision.revealstreams.persistence.domain.metadata.infra;

import java.io.Serializable;
import java.util.List;
import lombok.Data;


@Data
public class MetadataList implements Serializable {

  private List<MetadataObj> metadataObjs;
}
