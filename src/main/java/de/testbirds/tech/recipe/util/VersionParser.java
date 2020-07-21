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
package de.testbirds.tech.recipe.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse version information from an arbitrary version string. One important attribute of this class is
 * to never throw exceptions of any kind.
 *
 * @author testbirds
 */
public class VersionParser {

    /**
     * Match a major version number part.
     */
    private static final Pattern MAJOR_PATTERN = Pattern.compile("^[0-9]+\\.?");

    /**
     * The version as readable string, something like "1.2", "23", "2.54-rc1" or more exotic versions like "1.68b SP1".
     */
    private final String versionReadable;

    /**
     * Initialize the version parser to read version information from it.
     *
     * @param versionReadable the version as readable string, something like "1.2", "23", "2.54-rc1" or more exotic versions like
     *                        "1.68b SP1".
     */
    public VersionParser(final String versionReadable) {
        if (versionReadable == null) {
            this.versionReadable = "";
        } else {
            this.versionReadable = versionReadable;
        }
    }

    /**
     * Get the major version number if the version states a number or a number followed by '.'.
     *
     * @return the major version as integer or 0 in case it cannot be determined.
     */
    public final int getMajor() {
        return get(0);
    }

    /**
     * Get the minor version number if the version states a number followed by '.' and a second number.
     *
     * @return the minor version as integer or 0 in case it cannot be determined.
     */
    public final int getMinor() {
        return get(1);
    }

    /**
     * Searches for the number after n-1 dots and tries to parse it.
     *
     * @param index The index of the number.
     * @return The number as int.
     */
    public final int get(final int index) {
        int version = 0;
        String versionSubstring = versionReadable;
        try {
            for (int i = 0; i <= index; i++) {
                final Matcher matcher = MAJOR_PATTERN.matcher(versionSubstring);
                if (matcher.find()) {
                    final String matched = matcher.group();
                    final int indexOfDot = matched.indexOf('.');
                    if (indexOfDot > 0) {
                        version = Integer.parseInt(matched.substring(0, indexOfDot));
                    } else {
                        if (i == index) {
                            version = Integer.parseInt(matched);
                        } else {
                            version = 0;
                            break;
                        }
                    }
                    versionSubstring = versionSubstring.substring(matched.length());
                } else {
                    return 0;
                }
            }
        } catch (final NumberFormatException e) {
            version = 0;
        }
        return version;
    }

    @Override
    public final String toString() {
        return versionReadable;
    }
}
