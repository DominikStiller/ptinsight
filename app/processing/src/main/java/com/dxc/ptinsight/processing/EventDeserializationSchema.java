package com.dxc.ptinsight.processing;

import static com.dxc.ptinsight.proto.Base.Event;
import com.dxc.ptinsight.ProtobufSerializer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import static org.apache.flink.api.java.typeutils.TypeExtractor.getForClass;

public class EventDeserializationSchema implements KafkaDeserializationSchema<Event> {

    @Override
    public Event deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
        return ProtobufSerializer.deserialize(record.value(), "json");
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
