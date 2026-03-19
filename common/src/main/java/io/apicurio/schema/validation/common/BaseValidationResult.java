/*
 * Copyright 2026 Red Hat
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

package io.apicurio.schema.validation.common;

import java.util.List;

public class BaseValidationResult {

    private final boolean success;
    private final List<ValidationError> validationErrors;

    protected BaseValidationResult(boolean success, List<ValidationError> validationErrors) {
        this.success = success;
        this.validationErrors = validationErrors;
    }

    public boolean success() {
        return success;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

}
