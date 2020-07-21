package de.testbirds.tech.recipe;

import de.testbirds.tech.recipe.entity.Arch;
import de.testbirds.tech.recipe.entity.OSFamily;
import de.testbirds.tech.recipe.report.InvalidRecipeException;
import de.testbirds.tech.recipe.util.SystemStrings;
import de.testbirds.tech.recipe.util.URLBuilder;

/**
 * resolve constants that appear in Recipe2s. At the moment only DESKTOP and DELIMITER is supported, but there will be
 * more for example: PROGRAM_FOLDER, PROGRAM_FOLDER32, ... similar to %%Program%% on Windows but working on all os'
 *
 * @author testbirds
 */
public final class ConstantResolver {

    /**
     * returns the path to the desktop folder.
     */
    public static final String DESKTOP = "DESKTOP";

    /**
     * slash or backslash.
     */
    public static final String DELIMITER = "DELIMITER";

    /**
     * sw mirror where this startup should download software from.
     */
    public static final String SW_MIRROR = "SW_MIRROR";

    /**
     * the uuid of this vm.
     */
    public static final String UUID = "UUID";

    /**
     * Returns the path to the program files folder on windows. returns "C:\\Program Files" for 32 bit and "C:\\Program
     * Files (x86)" for 64 bit
     */
    private static final String PROGRAM_FILES_X86 = "PROGRAM_FILES_X86";

    /**
     * or in regex.
     */
    private static final char OR = '|';

    /**
     * the startup.
     */
    private final Startup startup;

    /**
     * static helper class.
     *
     * @param startup the startup
     */
    public ConstantResolver(final Startup startup) {
        this.startup = startup;
    }

    /**
     * return a pattern with all constants this class can resolve. remember that numbers, STD_OUT and STD_ERR can't be
     * used because of name conflicts.
     *
     * @return pattern: XYZ | DESKTOP | ...
     */
    public static String getRegexPattern() {
        return DESKTOP + OR + DELIMITER + OR + SW_MIRROR + OR + UUID + OR + PROGRAM_FILES_X86;
    }

    /**
     * the Recipe2 system allows to use special constants, that will be resolved by this method.
     *
     * @param constant constant name
     * @return the resolved constant
     * @throws InvalidRecipeException trying to resolve a constant that does not make sense
     */
    public String resolve(final String constant) throws InvalidRecipeException {
        final String answer;
        final OSFamily osFamily = startup.determineOSFamily();
        switch (constant) {
            case SW_MIRROR:
                answer = URLBuilder.getInst().getSoftwareMirror();
                break;
            case DESKTOP:
                answer = getDesktopPath(osFamily);
                break;
            case DELIMITER:
                answer = SystemStrings.fileSeparator();
                break;
            case UUID:
                answer = startup.getUUID().toString();
                break;
            case PROGRAM_FILES_X86:
                if (osFamily == OSFamily.WIN) {
                    if (startup.determineOSArch() == Arch.X86_64) {
                        answer = "C:\\Program Files (x86)\\";
                    } else {
                        answer = "C:\\Program Files\\";
                    }
                } else {
                    answer = "";
                }
                break;
            default:
                throw new InvalidRecipeException("unknown constant: " + constant);
        }
        return answer;

    }

    /**
     * Returns the desktop path for this os family.
     *
     * @param osFamily os family
     * @return path to desktop, ending with a trailing slash
     * @throws InvalidRecipeException we can't resolve desktop on this os family
     */
    private String getDesktopPath(final OSFamily osFamily) throws InvalidRecipeException {
        final String answer;
        switch (osFamily) {
            case WIN:
                final String osVersion = startup.determineOSVersion();
                if (osVersion.contains("5.1")) {
                    answer = "C:\\Documents and Settings\\testbirds\\Desktop\\";
                } else {
                    answer = "C:\\Users\\testbirds\\Desktop\\";
                }
                break;
            case MAC:
                answer = "/Users/testbirds/Desktop/";
                break;
            case IOS:
                answer = "/Users/testbirds/Desktop/";
                break;
            case UBUNTU:
            case FEDORA:
            case HUB:
            case TESTBIRDS:
                answer = "/home/testbirds/Desktop/";
                break;
            case ANDROID:
                answer = "/sdcard/Download/";
                break;
            default:
                throw new InvalidRecipeException("can't resolve DESKTOP on os " + osFamily);
        }
        return answer;
    }
}
