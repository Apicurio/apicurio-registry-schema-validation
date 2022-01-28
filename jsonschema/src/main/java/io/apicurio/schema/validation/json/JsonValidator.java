package io.apicurio.schema.validation.json;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import io.apicurio.registry.resolver.DefaultSchemaResolver;
import io.apicurio.registry.resolver.ParsedSchema;
import io.apicurio.registry.resolver.SchemaLookupResult;
import io.apicurio.registry.resolver.SchemaParser;
import io.apicurio.registry.resolver.SchemaResolver;
import io.apicurio.registry.resolver.data.Record;
import io.apicurio.registry.resolver.strategy.ArtifactReference;
import io.apicurio.registry.types.ArtifactType;

public class JsonValidator {
    
    private SchemaResolver<Schema, Object> schemaResolver;
    private ArtifactReference artifactReference;

    public JsonValidator(Map<String, Object> configuration, Optional<ArtifactReference> artifactReference) {
        this.schemaResolver = new DefaultSchemaResolver<>();
        this.schemaResolver.configure(configuration, new JsonSchemaParser());
        if (artifactReference.isPresent()) {
            this.artifactReference = artifactReference.get();
        }
    }

    protected JsonValidator() {
        //for tests
    }

    public JsonValidationResult validateByArtifactReference(Object bean) {
        Objects.requireNonNull(this.artifactReference, "ArtifactReference must be provided when creating JsonValidator in order to use this feature");
        SchemaLookupResult<Schema> schema = this.schemaResolver.resolveSchemaByArtifactReference(this.artifactReference);
        JSONObject jsonPayload = new JSONObject(bean);
        return validate(schema.getParsedSchema().getParsedSchema(), jsonPayload);
    }

    public JsonValidationResult validate(Record<Object> record) {
        SchemaLookupResult<Schema> schema = this.schemaResolver.resolveSchema(record);
        JSONObject jsonPayload = new JSONObject(record.payload());
        return validate(schema.getParsedSchema().getParsedSchema(), jsonPayload);
    }

    protected JsonValidationResult validate(Schema schema, JSONObject jsonPayload) {
        try {
            schema.validate(jsonPayload);
        } catch (ValidationException e) {
            return JsonValidationResult.fromErrors(extractValidationErrors(e));
        }
        return JsonValidationResult.SUCCESS;
    }

    private List<ValidationError> extractValidationErrors(ValidationException validationException) {
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationException> causes = validationException.getCausingExceptions();
        if (causes != null && !causes.isEmpty()) {
            for (ValidationException cause : causes) {
                errors.addAll(extractValidationErrors(cause));
            }
        } else {
            ValidationError error = new ValidationError(validationException.getMessage(), validationException.getPointerToViolation());
            errors.add(error);
        }
        return errors;
    }

    public static class JsonSchemaParser implements SchemaParser<Schema, Object> {
        @Override
        public ArtifactType artifactType() {
            return ArtifactType.JSON;
        }

        @Override
        public Schema parseSchema(byte[] rawSchema) {
            return SchemaLoader.load(new JSONObject(new JSONTokener(new ByteArrayInputStream(rawSchema))));
        }

        @Override
        public ParsedSchema<Schema> getSchemaFromData(Record<Object> data) {
            //not supported yet?
            return null;
        }

        @Override
        public boolean supportsExtractSchemaFromData() {
            return false;
        }
    }

}
