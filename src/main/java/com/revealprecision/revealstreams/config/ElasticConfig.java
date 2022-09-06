package com.revealprecision.revealstreams.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

@Configuration
public class ElasticConfig {

  @Value(value = "${elasticsearch.bootstrapAddress}")
  private String bootstrapAddress;

  @Bean
  public RestHighLevelClient client() {
    ClientConfiguration clientConfiguration
        = ClientConfiguration.builder()
        .connectedTo(bootstrapAddress)
        .withConnectTimeout(0)
        .withSocketTimeout(0)
        .build();

    return RestClients.create(clientConfiguration).rest();
  }

}