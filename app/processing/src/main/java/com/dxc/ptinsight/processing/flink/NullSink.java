package com.dxc.ptinsight.processing.flink;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class NullSink<IN> implements SinkFunction<IN> {}
