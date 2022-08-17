package com.revealprecision.revealstreams.messaging.serdes;

import com.revealprecision.revealstreams.messaging.message.Message;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Component
public class RevealSerdes {

  public <T extends Message> JsonSerde<T> get(Class<T> tClass) {
    JsonSerde<T> valueSerde = new JsonSerde<>(
        tClass);
    valueSerde.configure(
        Map.ofEntries(new SimpleEntry<>(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false"),
            new SimpleEntry<>(JsonDeserializer.TRUSTED_PACKAGES, "*")), false);
    return valueSerde;
  }

}
