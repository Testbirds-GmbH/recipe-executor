package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.CleanUpOperation;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.ProcessRunner;

import java.io.IOException;

/**
 * handle DMG_EULA.
 */
public class DmgEulaHandler extends AbstractRecipeMethodHandler {

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final String cdrFile = System.getProperty("java.io.tmpdir") + "tech.cdr";
        try {
            ProcessRunner.runCommand(new String[]{"hdiutil", "convert", parameter, "-format", "UDTO", "-o", cdrFile},
                    null);
        } catch (final IOException | InterruptedException e) {
            throw new SoftwareInstallException("error while converting dmg file", e);
        }
        return new StackElement(cdrFile, new CleanUpOperation(CleanUpOperation.Type.DELETE, cdrFile));
    }
}
