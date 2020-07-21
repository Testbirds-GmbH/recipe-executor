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
 * This exception is thrown if the software installation fails somehow.
 *
 * @author testbirds
 */
public class SoftwareInstallException extends Exception {

    /**
     * Exceptions must be serializable.
     */
    private static final long serialVersionUID = -7163894834381327427L;

    /**
     * Create install exception.
     *
     * @param reason reason of failure.
     * @param output the command output.
     */
    public SoftwareInstallException(final String reason, final String[] output) {
        super(reason + " -- o1: " + output[0] + " o2: " + output[1]);
    }

    /**
     * Create install exception without output.
     *
     * @param reason the reason of failure.
     */
    public SoftwareInstallException(final String reason) {
        super(reason);
    }

    /**
     * Create install exception with a child exception.
     *
     * @param reason the reason of failure.
     * @param cause  the exception that caused a failing software installation.
     */
    public SoftwareInstallException(final String reason, final Throwable cause) {
        super(reason, cause);
    }
}
