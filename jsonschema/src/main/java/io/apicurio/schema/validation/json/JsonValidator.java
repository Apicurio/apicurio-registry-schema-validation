/*
 * Copyright 2022 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.schema.validation.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
import io.apicurio.registry.resolver.SchemaResolverConfig;
import io.apicurio.registry.resolver.data.Record;
import io.apicurio.registry.resolver.strategy.ArtifactReference;
import io.apicurio.registry.types.ArtifactType;

/**
 * Provides validation APIs for JSON objects (Java objects, byte[], StringJSONObject,...) against a JSON Schema.
 * Schemas are managed in Apicurio Registry and downloaded and cached at runtime by this library.
 *
 * @author Fabian Martinez
 */
public class JsonValidator {

    private SchemaResolver<Schema, Object> schemaResolver;
    private ArtifactReference artifactReference;

    /**
     * Creates the JSON validator.
     * If artifactReference is provided it must exist in Apicurio Registry.
     * @param configuration , configuration properties for {@link DefaultSchemaResolver} for config properties see {@link SchemaResolverConfig}
     * @param artifactReference , optional {@link ArtifactReference} used as a static configuration to always use the same schema for validation when invoking {@link JsonValidator#validateByArtifactReference(Object)}
     */
    public JsonValidator(Map<String, Object> configuration, Optional<ArtifactReference> artifactReference) {
        this.schemaResolver = new DefaultSchemaResolver<>();
        this.schemaResolver.configure(configuration, new JsonSchemaParser());
        artifactReference.ifPresent(reference -> this.artifactReference = reference);
    }

    protected JsonValidator() {
        //for tests
    }

    /**
     * Validates the provided object against a JSON Schema.
     * The JSON Schema will be fetched from Apicurio Registry using the {@link ArtifactReference} provided in the constructor, this artifact must exist in the registry.
     * @param bean , the object that will be validate against the JSON Schema, can be a custom Java bean, String, byte[], InputStream, {@link JSONObject} or Map.
     * @return JsonValidationResult
     */
    public JsonValidationResult validateByArtifactReference(Object bean) {
        Objects.requireNonNull(this.artifactReference, "ArtifactReference must be provided when creating JsonValidator in order to use this feature");
        SchemaLookupResult<Schema> schema = this.schemaResolver.resolveSchemaByArtifactReference(this.artifactReference);
        JSONObject jsonPayload = createJSONObject(bean);
        return validate(schema.getParsedSchema().getParsedSchema(), jsonPayload);
    }

    /**
     * Validates the payload of the provided Record against a JSON Schema.
     * This method will resolve the schema based on the configuration provided in the constructor. See {@link SchemaResolverConfig} for configuration options and features of {@link SchemaResolver}.
     * You can use {@link JsonRecord} as the implementation for the provided record or you can use an implementation of your own.
     * Opposite to {@link JsonValidator#validateByArtifactReference(Object)} this method allow to dynamically use a different schema for validating each record.
     * @param record , the record used to resolve the schema used for validation and to provide the payload to validate.
     * @return JsonValidationResult
     */
    public JsonValidationResult validate(Record<Object> record) {
        SchemaLookupResult<Schema> schema = this.schemaResolver.resolveSchema(record);
        JSONObject jsonPayload = createJSONObject(record.payload());
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

    private JSONObject createJSONObject(Object bean) {
        if (bean instanceof JSONObject) {
            return (JSONObject) bean;
        }
        if (bean instanceof byte[]) {
            return new JSONObject(new JSONTokener(new ByteArrayInputStream((byte[]) bean)));
        }
        if (bean instanceof String) {
            return new JSONObject(new JSONTokener((String) bean));
        }
        if (bean instanceof InputStream) {
            return new JSONObject(new JSONTokener((InputStream) bean));
        }
        if (bean instanceof Map) {
            return new JSONObject((Map<?, ?>)bean);
        }
        return new JSONObject(bean);
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
        public String artifactType() {
            return ArtifactType.JSON;
        }

        @Override
        public Schema parseSchema(byte[] rawSchema,  Map<String, ParsedSchema<Schema>> references) {
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
