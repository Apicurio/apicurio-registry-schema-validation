package io.apicurio.schema.validation.json;

import io.apicurio.registry.resolver.data.Metadata;
import io.apicurio.registry.resolver.strategy.ArtifactReference;

public class JsonMetadata implements Metadata {

    private final ArtifactReference artifactReference;

    public JsonMetadata(ArtifactReference artifactReference) {
        this.artifactReference = artifactReference;
    }

    @Override
    public ArtifactReference artifactReference() {
        return this.artifactReference;
    }
    
}
