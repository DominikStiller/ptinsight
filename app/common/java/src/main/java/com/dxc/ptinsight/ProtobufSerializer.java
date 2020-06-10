package com.dxc.ptinsight;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.proto.Registry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class ProtobufSerializer {

  private static final JsonFormat.Printer jsonPrettyPrinter =
      JsonFormat.printer()
          .includingDefaultValueFields()
          .preservingProtoFieldNames()
          .usingTypeRegistry(Registry.INSTANCE);
  private static final JsonFormat.Printer jsonPrinter =
      jsonPrettyPrinter.omittingInsignificantWhitespace();
  private static final JsonFormat.Parser jsonParser =
      JsonFormat.parser().usingTypeRegistry(Registry.INSTANCE);

  public static byte[] serialize(Event event, String format) throws InvalidProtocolBufferException {
    switch (format) {
      case "json":
        return jsonPrinter.print(event).getBytes();
      case "json-pretty":
        return jsonPrettyPrinter.print(event).getBytes();
      case "binary":
        return event.toByteArray();
      default:
        throw new IllegalArgumentException("Unknown protobuf wire format");
    }
  }

  public static Event deserialize(byte[] msg, String format) throws InvalidProtocolBufferException {
    switch (format) {
      case "json":
      case "json-pretty":
        var builder = Event.newBuilder();
        jsonParser.merge(new String(msg), builder);
        return builder.build();
      case "binary":
        return Event.parseFrom(msg);
      default:
        throw new IllegalArgumentException("Unknown protobuf wire format");
    }
  }
}
