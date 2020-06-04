package com.dxc.ptinsight;

import java.nio.charset.StandardCharsets;

import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

public class ProducerStringSerializationSchema implements KafkaSerializationSchema<String> {

    private final String topic;

    public ProducerStringSerializationSchema(String topic) {
        super();
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(String element, Long timestamp) {
        return new ProducerRecord<byte[], byte[]>(topic, element.getBytes(StandardCharsets.UTF_8));
    }
}
