package io.apicurio.schema.validation.protobuf;

import com.google.protobuf.Message;
import io.apicurio.registry.resolver.data.Record;

public class ProtobufRecord<U extends Message> implements Record<U> {

    private final U payload;
    private final ProtobufMetadata metadata;

    public ProtobufRecord(U payload, ProtobufMetadata metadata) {
        this.payload = payload;
        this.metadata = metadata;
    }

    @Override
    public ProtobufMetadata metadata() {
        return this.metadata;
    }

    @Override
    public U payload() {
        return this.payload;
    }
}