package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.entity.Arch;
import de.testbirds.tech.recipe.entity.OSFamily;
import de.testbirds.tech.recipe.entity.RecipeMethod;
import de.testbirds.tech.recipe.entity.RecipeStep;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.VersionParser;

/**
 * move file into firefox profile folder.
 */
public class FirefoxProfileHandler extends AbstractRecipeMethodHandler {

    /**
     * a slash.
     */
    private static final String COMMENT = "\"";

    /**
     * Testbirds profile directory path.
     */
    private static final String TESTBIRDS_PROFILE_PATH = "Profiles{{DELIMITER}}testbirds.default{{DELIMITER}}";

    /**
     * put slashes around this string.
     *
     * @param content content
     * @return slashed content
     */
    private static String comment(final String content) {
        return COMMENT + content + COMMENT;
    }

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final OSFamily fam = exe.getStartup().determineOSFamily();
        final String ffPath;
        final String profilePath;
        switch (fam) {
            case WIN:
                ffPath = "{{PROGRAM_FILES_X86}}Mozilla Firefox\\firefox.exe";
                profilePath = "C:\\Users\\testbirds\\AppData\\Roaming\\Mozilla\\Firefox\\";
                createTestbirdsProfile(exe, parameter, ffPath, profilePath);
                maximizeOnWindows(exe, profilePath);
                break;
            case MAC:
                ffPath = "/Applications/Firefox.app/Contents/MacOS/firefox";
                profilePath = "/Users/testbirds/Library/Application Support/Firefox/";
                createTestbirdsProfile(exe, parameter, ffPath, profilePath);
                break;
            default:
                throw new SoftwareInstallException("putting files into the firefox profile not implemented on " + fam);
        }

        return new StackElement(profilePath);
    }

    /**
     * Create testbirds default profile.
     *
     * @param exe         the installer to execute the action
     * @param parameter   the recipe parameter
     * @param ffPath      the firefox executable directory path
     * @param profilePath the firefox profile directory path
     * @throws SoftwareInstallException if error occurs
     */
    private void createTestbirdsProfile(final Installer exe, final String parameter, final String ffPath,
                                        final String profilePath) throws SoftwareInstallException {
        final String target = profilePath + "profiles.ini";
        final RecipeStep.Builder builder = new RecipeStep.Builder()
                .cmd(comment(ffPath)
                        + " -CreateProfile \"testbirds " + profilePath + "Profiles{{DELIMITER}}testbirds.default\"")
                .pop()
                .add(RecipeMethod.TO_FILE,
                        String.format("[General]%nStartWithLastProfile=1%n%n[Profile0]%nName=default%n"
                                + "IsRelative=1%nPath=Profiles/testbirds.default%nDefault=1%n"))
                .move(target).pop().pop().move(profilePath + TESTBIRDS_PROFILE_PATH + parameter).pop();
        final String installation = getInstallation(exe);
        if (installation != null) {
            builder.add(RecipeMethod.TO_FILE,
                    String.format("[" + installation + "]%nDefault=Profiles/testbirds.default%nLocked=1%n"))
                    .move(profilePath + "installs.ini").pop().pop();
        }
        exe.insert(builder.build());
    }

    /**
     * Always start the firefox window maximized.
     *
     * @param exe         the installer to execute the action
     * @param profilePath the firefox profile path
     * @throws SoftwareInstallException if error occurs
     */
    private void maximizeOnWindows(final Installer exe, final String profilePath) throws SoftwareInstallException {
        final String fileName = "xulstore.json";
        exe.insert(new RecipeStep.Builder().add(RecipeMethod.TO_FILE, String.format(
                "{\"chrome://browser/content/browser.xul\":{\"main-window\":{\"screenX\":\"4\",\"screenY\":\"4\","
                        + "\"width\":\"1296\",\"height\":\"812\",\"sizemode\":\"maximized\"}}}"))
                .move(profilePath + TESTBIRDS_PROFILE_PATH + fileName).pop().pop().build());
    }

    /**
     * Gets the installs.ini section name.
     *
     * @param exe installer
     * @return section name
     */
    private String getInstallation(final Installer exe) {
        final String version = exe.getSoftware().getVersion();
        final Arch arch = exe.getSoftware().getArch();
        if (version == null || arch == null) {
            return null;
        }
        final OSFamily fam = exe.getStartup().determineOSFamily();
        String ans = null;
        final Integer firefoxProfileIssueSince = 67;
        final Integer firefoxVersion = new VersionParser(version).getMajor();
        if (firefoxVersion >= firefoxProfileIssueSince) {
            switch (fam) {
                case WIN:
                    if (arch == Arch.X86) {
                        ans = "E7CF176E110C211B";
                    } else {
                        ans = "308046B0AF4A39CB";
                    }
                    break;
                case MAC:
                    ans = "2656FF1E876E9973";
                    break;
                default:
                    break;
            }
        }
        return ans;
    }
}
