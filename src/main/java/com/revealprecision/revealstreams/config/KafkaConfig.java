package com.revealprecision.revealstreams.config;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME;

import com.revealprecision.revealstreams.messaging.Message;
import com.revealprecision.revealstreams.props.KafkaProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.streams.RecoveringDeserializationExceptionHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

@EnableKafka
@Configuration
@EnableKafkaStreams
@Slf4j
@RequiredArgsConstructor
public class KafkaConfig {

  private final KafkaProperties kafkaProperties;

  @Value(value = "${kafka.bootstrapAddress}") //TODO: test for multiple instances
  private String bootstrapAddress;

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopics setup() {

    List<NewTopic> topicsThatExpireMessages = kafkaProperties.getTopicNames().stream()
        .filter(topic -> kafkaProperties.getTopicConfigRetention().containsKey(topic))
        .map(topic -> TopicBuilder.name(topic).config(TopicConfig.RETENTION_MS_CONFIG,
            String.valueOf(kafkaProperties.getTopicConfigRetention().get(topic))).build())
        .collect(Collectors.toList());

    List<NewTopic> topicsThatDoNotExpireMessages = kafkaProperties.getTopicNames().stream()
        .filter(topic -> !kafkaProperties.getTopicConfigRetention().containsKey(topic))
        .map(topic -> TopicBuilder.name(topic)
            .build()
        ).collect(Collectors.toList());

    List<NewTopic> newTopics = new ArrayList<>();
    newTopics.addAll(topicsThatExpireMessages);
    newTopics.addAll(topicsThatDoNotExpireMessages);

    return new NewTopics(newTopics.toArray(NewTopic[]::new));
  }

  @Bean(name = DEFAULT_STREAMS_CONFIG_BEAN_NAME)
  KafkaStreamsConfiguration kStreamsConfig() {
    Map<String, Object> props = new HashMap<>();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaProperties.getApplicationId());
    props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(COMMIT_INTERVAL_MS_CONFIG, "5000");
    props.put(MAX_POLL_INTERVAL_MS_CONFIG,String.valueOf(45*60*1000));
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS,"false");
    props.put(JsonDeserializer.TRUSTED_PACKAGES,"*");
    return new KafkaStreamsConfiguration(props);
  }

}
