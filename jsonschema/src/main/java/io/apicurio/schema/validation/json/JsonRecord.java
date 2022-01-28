package io.apicurio.schema.validation.json;

import io.apicurio.registry.resolver.data.Record;

public class JsonRecord<T> implements Record<T> {

    private final T payload;
    private final JsonMetadata metadata;

    public JsonRecord(T payload, JsonMetadata metadata) {
        this.payload = payload;
        this.metadata = metadata;
    }

    @Override
    public JsonMetadata metadata() {
        return this.metadata;
    }

    @Override
    public T payload() {
        return this.payload;
    }
    
}
