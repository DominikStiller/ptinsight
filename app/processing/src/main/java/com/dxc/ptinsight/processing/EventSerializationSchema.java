package com.dxc.ptinsight.processing;

import com.dxc.ptinsight.Event;
import com.dxc.ptinsight.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

public class EventSerializationSchema implements KafkaSerializationSchema<Event> {

    private final String topic;

    public EventSerializationSchema(String topic) {
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(Event element, Long timestamp) {
        try {
            return new ProducerRecord<>(topic, JSON.getMapper().writeValueAsBytes(element));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
