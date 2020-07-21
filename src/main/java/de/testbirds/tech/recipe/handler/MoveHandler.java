package de.testbirds.tech.recipe.handler;

import com.google.common.io.Files;
import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * handle MOVE.
 */
public class MoveHandler extends AbstractRecipeMethodHandler {

    /**
     * the logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MoveHandler.class);

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final File source = new File(exe.getAdditionalParameter());
        File target = new File(parameter);
        try {
            LOGGER.debug("moving file from {} to {}", source, target);

            Files.createParentDirs(target);
            if (target.isDirectory() && !source.isDirectory()) {
                target = new File(target, source.getName());
            }

            Files.move(source, target);
            LOGGER.debug("finished moving file/folder");
        } catch (final IOException e) {
            throw new SoftwareInstallException("problem while moving files", e);
        }

        try {
            return new StackElement(target.getCanonicalPath());
        } catch (final IOException e) {
            throw new SoftwareInstallException("can't get the canonical path", e);
        }
    }
}
