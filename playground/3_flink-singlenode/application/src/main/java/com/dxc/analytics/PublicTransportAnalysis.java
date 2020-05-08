package com.dxc.analytics;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PublicTransportAnalysis {

    private static Logger log = LoggerFactory.getLogger(PublicTransportAnalysis.class);

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        var stream = env.addSource(new HSLMQTTSource("eda-demo", List.of("arr", "dep")));
        stream.print();

        env.execute("Public Transport Analysis");
    }
}
