package de.testbirds.tech.recipe.handler;

import com.google.common.io.Files;
import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * handle COPY.
 *
 * @author testbirds
 */
public class CopyHandler extends AbstractRecipeMethodHandler {

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final File source = new File(exe.getAdditionalParameter());
        File target = new File(parameter);
        try {
            Files.createParentDirs(target);

            if (target.isDirectory() && !source.isDirectory()) {
                target = new File(target, source.getName());
            }

            FileUtils.copyRecursive(source, target);
        } catch (final IOException e) {
            throw new SoftwareInstallException("problem while copying recursive", e);
        }

        try {
            return new StackElement(target.getCanonicalPath());
        } catch (final IOException e) {
            throw new SoftwareInstallException("can't get the canonical path", e);
        }
    }
}
