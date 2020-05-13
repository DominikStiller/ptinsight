package com.dxc.analytics;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class LambdaSink extends RichSinkFunction<String> {

    private AWSLambda client;
    private final String functionName;

    public LambdaSink(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        client = AWSLambdaClientBuilder.standard()
                                       .withCredentials(new DefaultAWSCredentialsProviderChain())
                                       .withRegion(Regions.EU_CENTRAL_1).build();
    }

    @Override
    public void invoke(String value, Context context) {
        var invokeRequest = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload("{\"value\":\"" + value +"\"}");
        client.invoke(invokeRequest);
    }
}
