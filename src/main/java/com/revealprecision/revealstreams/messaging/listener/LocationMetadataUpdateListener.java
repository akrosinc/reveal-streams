package com.revealprecision.revealstreams.messaging.listener;

import com.revealprecision.revealstreams.messaging.message.LocationMetadataEvent;
import com.revealprecision.revealstreams.persistence.es.EntityMetadataElastic;
import com.revealprecision.revealstreams.util.ElasticModelUtil;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Profile("Simulation")
@Slf4j
public class LocationMetadataUpdateListener extends Listener {

  @Value("${reveal.elastic.index-name}")
  String elasticIndex;

  private final RestHighLevelClient client;


  @KafkaListener(topics = "#{kafkaConfigProperties.topicMap.get('LOCATION_METADATA_AGGREGATE_UPDATE')}", groupId = "reveal_server_group", containerFactory = "kafkaBatchListenerContainerFactory")
  public void updateLocationMetadata(List<LocationMetadataEvent> message) throws IOException {
    log.info("Received Message in group foo: {}", message.toString());
    init();

    Map<String, List<LocationMetadataEvent>> metadatasById = message.stream()
        .collect(
            Collectors.groupingBy(
                locationMetadataEvent -> locationMetadataEvent.getEntityId().toString(),
                Collectors.mapping(locationMetadataEvent -> locationMetadataEvent,
                    Collectors.toList())));

    Map<String, List<EntityMetadataElastic>> collect1 = metadatasById.entrySet().stream()
        .map((entry) -> {
          List<EntityMetadataElastic> collect = entry.getValue().stream().flatMap(
                  locationMetadataEvent ->
                      locationMetadataEvent.getMetaDataEvents().stream()
                          .map(EntityMetadataElastic::new))
              .collect(
                  Collectors.toList());
          return new SimpleEntry<>(entry.getKey(), collect);
        }).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    for (Entry<String, List<EntityMetadataElastic>> entry : collect1.entrySet()) {

      Map<String, Object> parameters = new HashMap<>();

      List<Map<String, Object>> metadata = new ArrayList<>();
      for (EntityMetadataElastic entityMetadataElastic : entry.getValue()) {
        metadata.add(
            ElasticModelUtil.toMapFromPersonMetadata(entityMetadataElastic));
      }

      parameters.put("new_metadata", metadata);
      Script inline = new Script(ScriptType.INLINE, "painless",
          "ctx._source.metadata = params.new_metadata;", parameters);
      UpdateByQueryRequest request = new UpdateByQueryRequest(elasticIndex);
      request.setQuery(QueryBuilders.termQuery("_id", entry.getKey()));
      request.setConflicts("proceed");

      request.setScript(inline);
      log.debug("requesting elastic update location id {} with request {}", entry.getKey(),
          request);
      BulkByScrollResponse bulkByScrollResponse = null;

      bulkByScrollResponse = client.updateByQuery(request,
          RequestOptions.DEFAULT);

      if (bulkByScrollResponse.getVersionConflicts() > 0) {
        log.error("elastic update failed");
        throw new ElasticsearchException("Version conflict exception");
      }
      log.debug("Updated location id {} with response {}", entry.getKey(),
          bulkByScrollResponse.toString());
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
