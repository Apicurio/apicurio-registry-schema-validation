package io.apicurio.schema.validation.protobuf;

import com.google.protobuf.Message;
import io.apicurio.registry.protobuf.ProtobufDifference;
import io.apicurio.registry.resolver.*;
import io.apicurio.registry.resolver.data.Record;
import io.apicurio.registry.resolver.strategy.ArtifactReference;
import io.apicurio.registry.rules.compatibility.protobuf.ProtobufCompatibilityCheckerLibrary;
import io.apicurio.registry.utils.protobuf.schema.ProtobufFile;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;

import java.util.*;

/**
 * Provides validation APIs for Protobuf objects against a Protobuf Schema.
 * Schemas are managed in Apicurio Registry and downloaded and cached at runtime by this library.
 *
 * @author Carles Arnal
 */
public class ProtobufValidator {

    private final ProtobufSchemaParser<Message> protobufSchemaUSchemaParser;
    private SchemaResolver<ProtobufSchema, Message> schemaResolver;
    private ArtifactReference artifactReference;

    /**
     * Creates the Protobuf validator.
     * If artifactReference is provided it must exist in Apicurio Registry.
     *
     * @param configuration,     configuration properties for {@link DefaultSchemaResolver} for config properties see {@link SchemaResolverConfig}
     * @param artifactReference, optional {@link ArtifactReference} used as a static configuration to always use the same schema for validation when invoking validateArtifactByReference.
     */
    public ProtobufValidator(Map<String, Object> configuration,
            Optional<ArtifactReference> artifactReference) {
        this.schemaResolver = new DefaultSchemaResolver();
        this.protobufSchemaUSchemaParser = new ProtobufSchemaParser<>();
        this.schemaResolver.configure(configuration, protobufSchemaUSchemaParser);
        artifactReference.ifPresent(reference -> this.artifactReference = reference);
    }

    protected ProtobufValidator() {
        //for tests
        this.protobufSchemaUSchemaParser = new ProtobufSchemaParser<>();
    }

    /**
     * Validates the provided object against a Protobuf Schema.
     * The Protobuf Schema will be fetched from Apicurio Registry using the {@link ArtifactReference} provided in the constructor, this artifact must exist in the registry.
     *
     * @param bean , the object that will be validated against the Protobuf Schema, must implement {@link Message}.
     * @return ProtobufValidationResult
     */
    public ProtobufValidationResult validateByArtifactReference(Message bean) {
        Objects.requireNonNull(this.artifactReference,
                "ArtifactReference must be provided when creating JsonValidator in order to use this feature");
        SchemaLookupResult<ProtobufSchema> schema = this.schemaResolver.resolveSchemaByArtifactReference(
                this.artifactReference);
        return validate(schema.getParsedSchema(), new ProtobufRecord(bean, null));
    }

    /**
     * Validates the payload of the provided Record against a Protobuf Schema.
     * This method will resolve the schema based on the configuration provided in the constructor. See {@link SchemaResolverConfig} for configuration options and features of {@link SchemaResolver}.
     * You can use {@link ProtobufRecord} as the implementation for the provided record or you can use an implementation of your own.
     * Opposite to validateArtifactByReference this method allow to dynamically use a different schema for validating each record.
     *
     * @param record , the record used to resolve the schema used for validation and to provide the payload to validate.
     * @return ProtobufValidationResult
     */
    public ProtobufValidationResult validate(Record<Message> record) {
        SchemaLookupResult<ProtobufSchema> schema = this.schemaResolver.resolveSchema(record);
        return validate(schema.getParsedSchema(), record);
    }

    protected ProtobufValidationResult validate(ParsedSchema<ProtobufSchema> schema, Record<Message> record) {
        if (schema.getParsedSchema() != null && schema.getParsedSchema().getFileDescriptor()
                .findMessageTypeByName(record.payload().getDescriptorForType().getName()) == null) {

            return ProtobufValidationResult.fromErrors(List.of(new ValidationError(
                    "Missing message type " + record.payload().getDescriptorForType().getName()
                            + " in the protobuf schema", "")));
        }

        List<ProtobufDifference> diffs = validate(schema, record.payload());
        if (!diffs.isEmpty()) {
            List<ValidationError> validationErrors = new ArrayList<>();
            diffs.forEach(diff -> validationErrors.add(new ValidationError(diff.getMessage(), "")));
            return ProtobufValidationResult.fromErrors(validationErrors);
        }

        return ProtobufValidationResult.SUCCESS;
    }

    private List<ProtobufDifference> validate(ParsedSchema<ProtobufSchema> schemaFromRegistry, Message data) {
        ProtobufFile fileBefore = schemaFromRegistry.getParsedSchema().getProtobufFile();
        ProtobufFile fileAfter = new ProtobufFile(
                protobufSchemaUSchemaParser.toProtoFileElement(data.getDescriptorForType().getFile()));
        ProtobufCompatibilityCheckerLibrary checker = new ProtobufCompatibilityCheckerLibrary(fileBefore,
                fileAfter);
        return checker.findDifferences();
    }
}