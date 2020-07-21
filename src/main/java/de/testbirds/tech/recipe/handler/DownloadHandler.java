package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.CleanUpOperation;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.FileUtils;
import de.testbirds.tech.recipe.util.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * download handler.
 */
public final class DownloadHandler extends AbstractRecipeMethodHandler {

    /**
     * SLF4J logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DownloadHandler.class);

    /**
     * the download will start after this timestamp.
     */
    private final long waitUntil;

    /**
     * create a download handler.
     *
     * @param waitTill specify a timestamp. the download will wait until this timestamp is gone
     */
    public DownloadHandler(final long waitTill) {
        this.waitUntil = waitTill;
    }

    /**
     * create a downloader without timestamp it should wait for.
     */
    public DownloadHandler() {
        this.waitUntil = 0;
    }

    @Override
    public StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final URL url;
        try {
            // FIXME: did we ever use FTP?!! (that would be highly dangerous with some firewall issues)
            if (parameter.startsWith("http://") || parameter.startsWith("https://") || parameter.startsWith("ftp://")) {
                url = new URL(parameter);
            } else {
                url = URLBuilder.getInst().buildSoftwareDownload(parameter);
            }
        } catch (final MalformedURLException e) {
            throw new SoftwareInstallException("Download URL " + parameter + " cannot be handled.", e);
        }

        final File targetFile = createTempFile("tech", parameter.substring(parameter.lastIndexOf('/') + 1));
        for (int i = 0; true; i++) {
            try {
                final long timeToWait = waitUntil - System.currentTimeMillis();
                if (timeToWait > 0) {
                    LOG.debug("waiting {}ms until starting download", timeToWait);
                    Thread.sleep(timeToWait);
                }
                LOG.debug("Starting download of {}", url);
                FileUtils.writeInputStreamToFile(FileUtils.download(url), targetFile);
                break;
            } catch (final IOException | InterruptedException e) {
                LOG.warn("Exception during download", e);
                if (i >= 2) {
                    throw new SoftwareInstallException("exception during download of " + url, e);
                }
            }
        }

        // for Mac and Linux
        targetFile.setExecutable(true);

        return createStackElement(targetFile);
    }

    /**
     * creates the stack element for this target file.
     *
     * @param targetFile local file
     * @return stack element
     * @throws SoftwareInstallException error accessing the local file
     */
    private StackElement createStackElement(final File targetFile) throws SoftwareInstallException {
        try {
            final String path = targetFile.getCanonicalPath();
            return new StackElement(path, new CleanUpOperation(CleanUpOperation.Type.DELETE, path));
        } catch (final IOException e) {
            throw new SoftwareInstallException("exception while trying to get the canonical path", e);
        }

    }

    /**
     * creates a temp file.
     *
     * @param prefix prefix
     * @param suffix suffix
     * @return location of temp file
     * @throws SoftwareInstallException creating file went wrong
     */
    private File createTempFile(final String prefix, final String suffix) throws SoftwareInstallException {
        try {
            return File.createTempFile(prefix, suffix);
        } catch (final IOException e) {
            throw new SoftwareInstallException("can't create tmp file", e);
        }
    }
}
