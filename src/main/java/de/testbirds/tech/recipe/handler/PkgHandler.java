package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.ProcessRunner;

import java.io.IOException;

/**
 * handle PKG.
 */
public class PkgHandler extends AbstractRecipeMethodHandler {

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        try {
            final int exitCode = ProcessRunner
                    .runProcess(new String[]{"sudo", "installer", "-pkg", parameter, "-target", "/"}, null, null);
            return new StackElement(Integer.toString(exitCode));
        } catch (final IOException | InterruptedException e) {
            throw new SoftwareInstallException("error installing pkg file", e);
        }
    }
}
