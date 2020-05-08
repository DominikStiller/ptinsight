package com.dxc.analytics;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

public class HSLMQTTSource extends MQTTSource {

    public HSLMQTTSource(String clientid, Collection<String> events) {
        super();

        var properties = new Properties();
        properties.put("mqtt.server.url", "ssl://mqtt.hsl.fi:8883");
        properties.put("mqtt.client.id", clientid);
        properties.put("mqtt.topics", events.stream()
                .map((e) -> String.format("/hfp/v2/journey/ongoing/%s/+/+/+/+/+/+/+/+/0/#", e))
                .collect(Collectors.joining(",")));
        setProperties(properties);
    }
}
