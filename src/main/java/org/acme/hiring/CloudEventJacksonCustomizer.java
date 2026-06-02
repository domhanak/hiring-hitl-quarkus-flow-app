package org.acme.hiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class CloudEventJacksonCustomizer implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        // Teaches Jackson how to construct the CloudEvent interface
        mapper.registerModule(JsonFormat.getCloudEventJacksonModule());
    }
}