package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.entity.OSFamily;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.ProcessRunner;

import java.io.IOException;

/**
 * handle the RUN recipe method.
 */
public class RunHandler extends AbstractRecipeMethodHandler {

    /**
     * create a new run handler.
     */
    public RunHandler() {
    }

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final String[] output = new String[2];
        final int exitCode;
        try {
            // the desktop case. if you want another behavior, set another handler
            final String[] script = new String[]{parameter};
            if (exe.getStartup().determineOSFamily() == OSFamily.WIN) {
                exitCode = ProcessRunner.runBatchScript(script, output);
            } else {
                exitCode = ProcessRunner.runBashScript(script, output);
            }
        } catch (final IOException | InterruptedException e) {
            throw new SoftwareInstallException("error executing the command", e);
        }

        return new StackElement(Integer.toString(exitCode), null, output[0], output[1]);
    }
}
