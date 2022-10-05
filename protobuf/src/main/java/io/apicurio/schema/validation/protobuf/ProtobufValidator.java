package io.apicurio.schema.validation.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.apicurio.registry.resolver.*;
import io.apicurio.registry.resolver.data.Record;
import io.apicurio.registry.resolver.strategy.ArtifactReference;
import io.apicurio.registry.resolver.utils.Utils;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;
import io.apicurio.schema.validation.protobuf.ref.RefOuterClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides validation APIs for Protobuf objects (Java objects, byte[],...) against a Protobuf Schema.
 * Schemas are managed in Apicurio Registry and downloaded and cached at runtime by this library.
 *
 * @author Carles Arnal
 */
public class ProtobufValidator<U extends Message> {

    private SchemaResolver<ProtobufSchema, U> schemaResolver;
    private ArtifactReference artifactReference;
    private final Map<String, Method> parseMethodsCache = new ConcurrentHashMap<>();
    private static final String PROTOBUF_PARSE_METHOD = "parseFrom";

    /**
     * Creates the JSON validator.
     * If artifactReference is provided it must exist in Apicurio Registry.
     *
     * @param configuration,     configuration properties for {@link DefaultSchemaResolver} for config properties see {@link SchemaResolverConfig}
     * @param artifactReference, optional {@link ArtifactReference} used as a static configuration to always use the same schema for validation when invoking {@link ProtobufValidator#validateByArtifactReference(U)}
     */
    public ProtobufValidator(Map<String, Object> configuration, Optional<ArtifactReference> artifactReference,
            Class<?> parsedType) {
        this.schemaResolver = new DefaultSchemaResolver();
        this.schemaResolver.configure(configuration, new ProtobufSchemaParser());
        artifactReference.ifPresent(reference -> this.artifactReference = reference);
    }

    protected ProtobufValidator() {
        //for tests
    }

    /**
     * Validates the provided object against a Protobuf Schema.
     * The Protobuf Schema will be fetched from Apicurio Registry using the {@link ArtifactReference} provided in the constructor, this artifact must exist in the registry.
     *
     * @param bean , the object that will be validate against the Protobuf Schema, can be a custom Java bean, String, byte[], InputStream, or Map.
     * @return ProtobufValidationResult
     */
    public ProtobufValidationResult validateByArtifactReference(U bean) {
        Objects.requireNonNull(this.artifactReference,
                "ArtifactReference must be provided when creating JsonValidator in order to use this feature");
        SchemaLookupResult<ProtobufSchema> schema = this.schemaResolver.resolveSchemaByArtifactReference(
                this.artifactReference);
        return validate(schema.getParsedSchema(), new ProtobufRecord<>(bean, null));
    }

    /**
     * Validates the payload of the provided Record against a Protobuf Schema.
     * This method will resolve the schema based on the configuration provided in the constructor. See {@link SchemaResolverConfig} for configuration options and features of {@link SchemaResolver}.
     * You can use {@link ProtobufRecord} as the implementation for the provided record or you can use an implementation of your own.
     * Opposite to {@link ProtobufValidator#validateByArtifactReference(U)} this method allow to dynamically use a different schema for validating each record.
     *
     * @param record , the record used to resolve the schema used for validation and to provide the payload to validate.
     * @return ProtobufValidationResult
     */
    public ProtobufValidationResult validate(ProtobufRecord<U> record) {
        SchemaLookupResult<ProtobufSchema> schema = this.schemaResolver.resolveSchema(record);
        return validate(schema.getParsedSchema(), record);
    }

    protected ProtobufValidationResult validate(ParsedSchema<ProtobufSchema> schema, Record<U> record) {

        byte[] bytes = record.payload().toByteArray();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Descriptors.Descriptor descriptor;

        try {
            RefOuterClass.Ref ref = RefOuterClass.Ref.parseDelimitedFrom(is);
            descriptor = schema.getParsedSchema().getFileDescriptor().findMessageTypeByName(ref.getName());
        } catch (IOException e) {
            is = new ByteArrayInputStream(bytes);
            //use the first message type found
            descriptor = schema.getParsedSchema().getFileDescriptor().getMessageTypes().get(0);
        }

        String className = deriveClassFromDescriptor(descriptor);
        if (className != null) {
            final U resultParsed = invokeParseMethod(is, className);
            if (record.payload().getClass().equals(resultParsed.getClass())) {
                return ProtobufValidationResult.SUCCESS;
            }
        } else {
            return ProtobufValidationResult.fromErrors(List.of(new ValidationError()));
        }
        return ProtobufValidationResult.fromErrors(List.of(new ValidationError()));
    }

    private U invokeParseMethod(InputStream buffer, String className) {
        try {
            Method parseMethod = parseMethodsCache.computeIfAbsent(className, k -> {
                Class<?> protobufClass = Utils.loadClass(className);
                try {
                    return protobufClass.getDeclaredMethod(PROTOBUF_PARSE_METHOD, InputStream.class);
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new IllegalArgumentException(
                            "Class " + className + " is not a valid protobuf message class", e);
                }
            });
            return (U) parseMethod.invoke(null, buffer);
        } catch (IllegalAccessException | InvocationTargetException e) {
            parseMethodsCache.remove(className);
            throw new IllegalArgumentException("Not a valid protobuf builder", e);
        }
    }

    public String deriveClassFromDescriptor(Descriptors.Descriptor des) {
        Descriptors.Descriptor descriptor = des;
        Descriptors.FileDescriptor fd = descriptor.getFile();
        DescriptorProtos.FileOptions o = fd.getOptions();
        String p = o.hasJavaPackage() ? o.getJavaPackage() : fd.getPackage();
        String outer = "";
        if (!o.getJavaMultipleFiles()) {
            if (o.hasJavaOuterClassname()) {
                outer = o.getJavaOuterClassname();
            } else {
                // Can't determine full name without either java_outer_classname or java_multiple_files
                return null;
            }
        }
        StringBuilder inner = new StringBuilder();
        while (descriptor != null) {
            if (inner.length() == 0) {
                inner.insert(0, descriptor.getName());
            } else {
                inner.insert(0, descriptor.getName() + "$");
            }
            descriptor = descriptor.getContainingType();
        }
        String d1 = (!outer.isEmpty() || inner.length() != 0 ? "." : "");
        String d2 = (!outer.isEmpty() && inner.length() != 0 ? "$" : "");
        return p + d1 + outer + d2 + inner;
    }
}