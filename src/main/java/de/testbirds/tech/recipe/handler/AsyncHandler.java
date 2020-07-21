package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.entity.OSFamily;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.ProcessRunner;

import java.io.File;
import java.io.IOException;

/**
 * handle RecipeMethod#ASYNC.
 */
public class AsyncHandler extends AbstractRecipeMethodHandler {

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final String[] script = new String[]{parameter};
        final OSFamily fam = exe.getStartup().determineOSFamily();

        try {
            if (fam == OSFamily.WIN) {
                final File file = ProcessRunner.writeBatchScript(script);
                ProcessRunner.startProcess(new String[]{file.getAbsolutePath()}, null);
            } else {
                ProcessRunner.startProcess(new String[]{"bash", "-s", "-e"}, script);
            }
        } catch (final IOException e) {
            throw new SoftwareInstallException("can't run command async ", e);
        }
        return new StackElement("");
    }
}
