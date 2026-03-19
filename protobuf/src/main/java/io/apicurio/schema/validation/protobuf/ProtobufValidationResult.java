package io.apicurio.schema.validation.protobuf;

import io.apicurio.schema.validation.common.BaseValidationResult;
import io.apicurio.schema.validation.common.ValidationError;

import java.util.List;

public class ProtobufValidationResult extends BaseValidationResult {

    protected static final ProtobufValidationResult SUCCESS = successful();

    private ProtobufValidationResult(boolean success, List<ValidationError> validationErrors) {
        super(success, validationErrors);
    }

    @Override
    public String toString() {
        if (this.success()) {
            return "ProtobufValidationResult [ success ]";
        } else {
            return "ProtobufValidationResult [ errors = " + getValidationErrors().toString() + " ]";
        }
    }

    public static ProtobufValidationResult fromErrors(List<ValidationError> errors) {
        return new ProtobufValidationResult(errors == null || errors.isEmpty(), errors);
    }

    public static ProtobufValidationResult successful() {
        return new ProtobufValidationResult(true, null);
    }
}
