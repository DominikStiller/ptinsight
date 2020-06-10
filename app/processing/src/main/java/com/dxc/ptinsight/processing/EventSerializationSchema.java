package com.dxc.ptinsight.processing;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.ProtobufSerializer;
import com.google.protobuf.InvalidProtocolBufferException;
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
      return new ProducerRecord<>(topic, ProtobufSerializer.serialize(element, "json"));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
  }
}
