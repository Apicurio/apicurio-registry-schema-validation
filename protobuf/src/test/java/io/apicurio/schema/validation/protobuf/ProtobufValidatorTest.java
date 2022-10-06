package io.apicurio.schema.validation.protobuf;

import io.apicurio.registry.resolver.ParsedSchemaImpl;
import io.apicurio.registry.resolver.data.Record;
import io.apicurio.registry.utils.IoUtil;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;
import io.apicurio.schema.validation.protobuf.ref.MessageExample2OuterClass;
import io.apicurio.schema.validation.protobuf.ref.MessageExample2OuterClass.MessageExample2;
import io.apicurio.schema.validation.protobuf.ref.MessageExampleOuterClass;
import io.apicurio.schema.validation.protobuf.ref.MessageExampleOuterClass.MessageExample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ProtobufValidatorTest {

    @Test
    public void testValidMessage() {
        ProtobufValidator validator = new ProtobufValidator();

        MessageExample messageExample = MessageExample.newBuilder()
                .setKey("testValidMessageKey")
                .setValue("testValidMessageValue").build();
        ProtobufSchemaParser<MessageExample> protobufSchemaParser = new ProtobufSchemaParser<>();

        final byte[] schemaBytes = readResource("message_example.proto");
        final ProtobufSchema protobufSchema = protobufSchemaParser.parseSchema(schemaBytes, Collections.emptyMap());

        ParsedSchemaImpl<ProtobufSchema> ps = new ParsedSchemaImpl<ProtobufSchema>().setParsedSchema(
                protobufSchema).setRawSchema(schemaBytes);

        Record protobufRecord = new ProtobufRecord(messageExample, null);

        final ProtobufValidationResult result = validator.validate(ps, protobufRecord);

        assertTrue(result.success());
    }

    @Test
    public void testInvalidMessage() {
        ProtobufValidator validator = new ProtobufValidator();

        MessageExample2 messageExample = MessageExample2.newBuilder()
                .setKey2("testValidMessageKey")
                .setValue2(23).build();

        ProtobufSchemaParser<MessageExample> protobufSchemaParser = new ProtobufSchemaParser<>();
        final byte[] schemaBytes = readResource("message_example.proto");

        final ProtobufSchema protobufSchema = protobufSchemaParser.parseSchema(schemaBytes, Collections.emptyMap());
        ParsedSchemaImpl<ProtobufSchema> ps = new ParsedSchemaImpl<ProtobufSchema>().setParsedSchema(
                protobufSchema).setRawSchema(schemaBytes);

        Record protobufRecord = new ProtobufRecord(messageExample, null);

        final ProtobufValidationResult result = validator.validate(ps, protobufRecord);

        assertFalse(result.success());
        assertNotNull(result.getValidationErrors());
    }

    public static byte[] readResource(String resourceName) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            Assertions.assertNotNull(stream, "Resource not found: " + resourceName);
            return IoUtil.toBytes(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}