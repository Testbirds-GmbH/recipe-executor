package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * handle READ_FILE.
 */
public class ReadFileHandler extends AbstractRecipeMethodHandler {

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final File source = new File(parameter);
        try {
            final String content = FileUtils.readFileToString(source);
            return new StackElement(content);
        } catch (final IOException e) {
            throw new SoftwareInstallException("can't read file", e);
        }
    }
}
