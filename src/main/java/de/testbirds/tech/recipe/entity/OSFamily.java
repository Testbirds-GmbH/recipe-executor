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
package de.testbirds.tech.recipe.entity;

/**
 * OS family enumeration.
 *
 * @author testbirds
 */
public enum OSFamily {

    /**
     * Microsoft Windows.
     */
    WIN,

    /**
     * Mac OS X.
     */
    MAC,

    /**
     * Linux (currently Debian).
     *
     * @deprecated use UBUNTU instead
     */
    @Deprecated
    LINUX,

    /**
     * Ubuntu Linux.
     */
    UBUNTU,

    /**
     * Debian GNU/Linux.
     *
     * @deprecated we don't offer Debian any more.
     */
    @Deprecated
    DEBIAN,

    /**
     * Fedora Linux.
     *
     * @deprecated we don't offer Fedora any more.
     */
    @Deprecated
    FEDORA,

    /**
     * Google Android.
     */
    ANDROID,

    /**
     * Apple iOS.
     */
    IOS,

    /**
     * Internal Hubs.
     */
    HUB,

    /**
     * Special Testbirds images only for TeCh developers.
     */
    TESTBIRDS,

    /**
     * Unknown OS family.
     */
    UNKNOWN;

    /**
     * Check if the given OS family is a hub family.
     *
     * @param family the family to check.
     * @return true iff it is a hub OS family.
     */
    public static boolean isHub(final OSFamily family) {
        return family == OSFamily.LINUX || family == OSFamily.HUB;
    }
}
