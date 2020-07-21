package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.CleanUpOperation;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * handle TO_FILE.
 */
public class WriteFileHandler extends AbstractRecipeMethodHandler {

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        try {
            final File target = File.createTempFile("techw", "");
            FileUtils.writeStringToFile(parameter, target);
            target.setExecutable(true);
            return new StackElement(target.getCanonicalPath(),
                    new CleanUpOperation(CleanUpOperation.Type.DELETE, target.getCanonicalPath()));
        } catch (final IOException e) {
            throw new SoftwareInstallException("can't write string to file", e);
        }
    }
}
