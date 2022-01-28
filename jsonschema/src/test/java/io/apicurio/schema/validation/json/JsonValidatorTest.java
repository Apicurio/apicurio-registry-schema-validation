package io.apicurio.schema.validation.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.apicurio.registry.utils.IoUtil;

public class JsonValidatorTest {
    
    @Test
    public void testValidMessage() {
        JsonValidator validator = new JsonValidator();

        JSONObject jsonPayload = createTestMessageBean();

        Schema validSchema = createSchemaFromResource("message.json");

        var result = validator.validate(validSchema, jsonPayload);

        assertTrue(result.success());
        assertNull(result.getValidationErrors());
    }

    @Test
    public void testInvalidMessage() {
        JsonValidator validator = new JsonValidator();

        JSONObject jsonPayload = createTestMessageBean();

        Schema invalidSchema = createSchemaFromResource("message-invalid.json");

        var result = validator.validate(invalidSchema, jsonPayload);

        assertFalse(result.success());
        assertNotNull(result.getValidationErrors());
        assertFalse(result.getValidationErrors().isEmpty());
        assertEquals(1, result.getValidationErrors().size());
        assertEquals(1, result.getValidationErrors().stream().filter(ve -> "#/time".equals(ve.getContext())).count());
    }

    @Test
    public void testInvalidMessageMultipleErrors() {
        JsonValidator validator = new JsonValidator();

        JSONObject jsonPayload = createTestMessageBean();

        Schema invalidSchema = createSchemaFromResource("message-invalid-multi.json");

        var result = validator.validate(invalidSchema, jsonPayload);

        assertFalse(result.success());
        assertNotNull(result.getValidationErrors());
        assertFalse(result.getValidationErrors().isEmpty());
        assertEquals(2, result.getValidationErrors().size());
        assertEquals(1, result.getValidationErrors().stream().filter(ve -> "#/time".equals(ve.getContext())).count());
        assertEquals(1, result.getValidationErrors().stream().filter(ve -> "#".equals(ve.getContext())).count());
    }

    private JSONObject createTestMessageBean() {
        TestMessageBean message = new TestMessageBean();
        message.setMessage("hello");
        message.setTime(System.currentTimeMillis());
        JSONObject jsonPayload = new JSONObject(message);
        return jsonPayload;
    }

    private Schema createSchemaFromResource(String resource) {
        return SchemaLoader.load(new JSONObject(new JSONTokener(new ByteArrayInputStream(readResource(resource)))));
    }

    public static byte[] readResource(String resourceName) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            Assertions.assertNotNull(stream, "Resource not found: " + resourceName);
            return IoUtil.toBytes(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
