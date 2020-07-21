package de.testbirds.tech.recipe.util;

/**
 * System specific strings like line or path seperators (Java 6 compatible etc.).
 * <p>
 *
 * @author testbirds
 */
public final class SystemStrings {
    /**
     * The line separator for system files and output.
     *
     * @return (undocumented)
     */
    static public String lineSeparator() {
        throw new RuntimeException();
    }

    /**
     * The Windows line separator "\r\n".
     *
     * @return (undocumented)
     */
    static public String windowsLineSeparator() {
        throw new RuntimeException();
    }

    /**
     * The Unix line separator "\n".
     *
     * @return (undocumented)
     */
    static public String unixLineSeparator() {
        throw new RuntimeException();
    }

    /**
     * The Mac line separator "\r".
     *
     * @return (undocumented)
     */
    static public String macLineSeparator() {
        throw new RuntimeException();
    }

    /**
     * The separator used in paths to separate files from their parent directory. Usually "/".
     *
     * @return (undocumented)
     */
    static public String fileSeparator() {
        throw new RuntimeException();
    }
}
