package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.CleanUpOperation;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * handles the UNZIP Recipe Method.
 */
public class UnzipHandler extends AbstractRecipeMethodHandler {

    /**
     * the logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UnzipHandler.class);

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        // create a tmp folder
        final File targetDir;
        try {
            targetDir = File.createTempFile("techzip", "");
        } catch (final IOException e) {
            throw new SoftwareInstallException("can't create tmp file", e);
        }
        // not sure why this was necessary?
        if (!targetDir.delete()) {
            LOG.error("Couldn't delete file before unzipping");
        }
        if (!targetDir.mkdirs()) {
            LOG.error("Couldn't create file before unzipping");
        }
        try {
            final String dirName = targetDir.getCanonicalPath();
            FileUtils.unzip(new File(parameter), dirName);

            return new StackElement(dirName, new CleanUpOperation(CleanUpOperation.Type.DELETE, dirName));
        } catch (final IOException e) {
            throw new SoftwareInstallException("exception while unzipping", e);
        }

    }
}
