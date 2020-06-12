package com.dxc.ptinsight.processing.flink;

import static com.dxc.ptinsight.proto.Base.Event;
import static org.apache.flink.api.java.typeutils.TypeExtractor.getForClass;

import com.dxc.ptinsight.ProtobufSerializer;
import com.dxc.ptinsight.processing.EntryPoint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class EventDeserializationSchema implements KafkaDeserializationSchema<Event> {

  @Override
  public Event deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
    return ProtobufSerializer.deserialize(
        record.value(), EntryPoint.getConfiguration().kafka.protobufFormat);
  }

  @Override
  public boolean isEndOfStream(Event nextElement) {
    return false;
  }

  @Override
  public TypeInformation<Event> getProducedType() {
    return getForClass(Event.class);
  }
}
