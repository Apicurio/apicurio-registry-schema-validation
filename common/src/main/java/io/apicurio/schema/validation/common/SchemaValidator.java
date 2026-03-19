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

import io.apicurio.registry.resolver.data.Record;

import java.io.Closeable;

/**
 * Common interface for all schema validators.
 *
 * @param <T> the type of data being validated
 * @param <R> the type of validation result returned
 */
public interface SchemaValidator<T, R extends BaseValidationResult> extends Closeable {

    /**
     * Validates the provided object against a schema fetched from Apicurio Registry
     * using the ArtifactReference configured at construction time.
     *
     * @param bean the object to validate
     * @return the validation result
     */
    R validateByArtifactReference(T bean);

    /**
     * Validates the payload of the provided Record against a schema resolved dynamically.
     *
     * @param record the record containing the data and metadata for schema resolution
     * @return the validation result
     */
    R validate(Record<T> record);

    /**
     * Resets the schema cache, forcing schemas to be re-fetched from the registry
     * on the next validation.
     */
    void reset();

}
