/*
 * Copyright 2019 Testbirds GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.testbirds.tech.recipe.report;

/**
 * This exception is thrown if the recipe somehow is not consistent (it accesses a non existing stack item, or uses a
 * constant not defined).
 *
 * @author testbirds
 */
public class InvalidRecipeException extends SoftwareInstallException {

    /**
     * Exceptions must be serializable.
     */
    private static final long serialVersionUID = 3201344514753464697L;

    /**
     * Create new exception.
     *
     * @param reason the reason of failure.
     */
    public InvalidRecipeException(final String reason) {
        super(reason);
    }

    /**
     * Create invalid recipe exception with a child exception.
     *
     * @param reason the reason of failure.
     * @param cause  the exception that is the reason for the recipe to be illegal.
     */
    public InvalidRecipeException(final String reason, final Throwable cause) {
        super(reason, cause);
    }
}
