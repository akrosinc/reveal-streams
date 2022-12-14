package com.revealprecision.revealstreams.messaging.listener;

import com.revealprecision.revealstreams.messaging.message.LocationMetadataEvent;
import com.revealprecision.revealstreams.messaging.message.MetaDataEvent;
import com.revealprecision.revealstreams.persistence.es.EntityMetadataElastic;
import com.revealprecision.revealstreams.util.ElasticModelUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Profile("Simulation")
@Slf4j
public class LocationMetadataUpdateListener extends Listener{

  private final RestHighLevelClient client;


  @KafkaListener(topics = "#{kafkaConfigProperties.topicMap.get('LOCATION_METADATA_AGGREGATE_UPDATE')}", groupId = "reveal_server_group")
  public void updateLocationMetadata(LocationMetadataEvent message) throws IOException {
    log.info("Received Message in group foo: {}" , message.toString());
    init();

    Map<String, Object> parameters = new HashMap<>();
    List<Map<String, Object>> metadata = new ArrayList<>();
    for(MetaDataEvent metadataObj : message.getMetaDataEvents()) {
      metadata.add(ElasticModelUtil.toMapFromPersonMetadata(new EntityMetadataElastic(metadataObj)));
    }
    parameters.put("new_metadata", metadata);
    Script inline = new Script(ScriptType.INLINE, "painless",
        "ctx._source.metadata = params.new_metadata;",parameters);
    UpdateByQueryRequest request = new UpdateByQueryRequest("location");
    request.setQuery(QueryBuilders.termQuery("_id", message.getEntityId().toString()));
    request.setConflicts("proceed");
    request.setScript(inline);
    log.debug("requesting elastic update location id {} with request {}",message.getEntityId(),request);
    BulkByScrollResponse bulkByScrollResponse = client.updateByQuery(request,
        RequestOptions.DEFAULT);
    if(bulkByScrollResponse.getVersionConflicts() > 0) {
      log.error("elastic update failed");
      throw new ElasticsearchException("Version conflict exception");
    }
    log.debug("Updated location id {} with response {}",message.getEntityId(),bulkByScrollResponse.toString());
  }
}
