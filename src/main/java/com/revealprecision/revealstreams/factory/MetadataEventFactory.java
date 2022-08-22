package com.revealprecision.revealstreams.factory;


import com.revealprecision.revealstreams.messaging.message.MetaDataEvent;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.MetadataObj;

public class MetadataEventFactory {

  public static MetaDataEvent getMetaDataEvent(MetadataObj metadataObj) {
    MetaDataEvent metaDataEvent = new MetaDataEvent();
    metaDataEvent.setTag(metadataObj.getTag());
    metaDataEvent.setTagData(metadataObj.getCurrent());
    metaDataEvent.setEntityTagId(metadataObj.getEntityTagId());
    metaDataEvent.setActive(metadataObj.isActive());
    metaDataEvent.setType(metadataObj.getType());
    metaDataEvent.setDataType(metadataObj.getDataType());
    metaDataEvent.setDateScope(metadataObj.isDateScope());
    metaDataEvent.setDateForDateScope(metadataObj.getDateForDateScope());
    metaDataEvent.setCaptureNumber(metadataObj.getCaptureNumber());
    return metaDataEvent;
  }

}
