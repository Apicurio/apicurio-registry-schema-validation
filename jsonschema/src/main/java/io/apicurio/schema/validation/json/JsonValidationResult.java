package io.apicurio.schema.validation.json;

import java.util.List;

public class JsonValidationResult {
    
    protected static final JsonValidationResult SUCCESS = successfull();

    private boolean success;
    private List<ValidationError> validationErrors;

    private JsonValidationResult(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
        this.success = this.validationErrors == null || this.validationErrors.isEmpty();
    }
    
    public boolean success() {
        return success;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    @Override
    public String toString() {
        if (this.success) {
            return "JsonValidationResult [ success ]";
        } else {
            return "JsonValidationResult [ errors = " + validationErrors.toString() + " ]";
        }
    }

    public static JsonValidationResult fromErrors(List<ValidationError> errors) {
        return new JsonValidationResult(errors);
    }

    public static JsonValidationResult successfull() {
        return new JsonValidationResult(null);
    }

}
