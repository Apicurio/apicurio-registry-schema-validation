package io.apicurio.schema.validation.protobuf;

import io.apicurio.registry.resolver.ParsedSchemaImpl;
import io.apicurio.registry.resolver.data.Record;
import io.apicurio.registry.utils.IoUtil;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;

import static io.apicurio.schema.validation.protobuf.ref.MessageExampleOuterClass.MessageExample;

public class ProtobufValidatorTest {

    @Test
    public void testValidMessage() {
        ProtobufValidator<MessageExample> validator = new ProtobufValidator<>();

        MessageExample messageExample = MessageExample.newBuilder()
                .setKey("testValidMessageKey")
                .setValue("testValidMessageValue").build();
        ProtobufSchemaParser<MessageExample> protobufSchemaParser = new ProtobufSchemaParser<>();

        final byte[] schemaBytes = readResource("message_example.proto");

        final ProtobufSchema protobufSchema = protobufSchemaParser.parseSchema(schemaBytes, Collections.emptyMap());

        ParsedSchemaImpl<ProtobufSchema> ps = new ParsedSchemaImpl<ProtobufSchema>().setParsedSchema(
                protobufSchema).setRawSchema(schemaBytes);

        Record<MessageExample> protobufRecord = new ProtobufRecord<>(messageExample, null);

        validator.validate(ps, protobufRecord);
    }

    @Test
    public void testInvalidMessage() {

    }

    @Test
    public void testInvalidMessageMultipleErrors() {

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