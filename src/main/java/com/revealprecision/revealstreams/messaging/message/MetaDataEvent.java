package com.revealprecision.revealstreams.messaging.message;

import com.revealprecision.revealstreams.persistence.domain.metadata.infra.TagData;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MetaDataEvent extends Message {

  private UUID entityTagId;

  private String tag;

  private TagData tagData;

  private String dataType;

  private String type;

  private boolean isActive;

  private boolean dateScope;

  private String dateForDateScope;

  private Long captureNumber;

  private String countKey;
}
