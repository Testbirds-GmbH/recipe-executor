package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.CleanUpOperation;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.ProcessRunner;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * handle SET.
 */
public class DmgHandler extends AbstractRecipeMethodHandler {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final String mountDir = "/Volumes/techmount" + COUNTER.getAndIncrement();
        // mount
        try {
            ProcessRunner.runCommand(
                    new String[]{"hdiutil", "attach", parameter, "-mountpoint", mountDir, "-nobrowse", "-quiet"},
                    null);
        } catch (final IOException | InterruptedException e) {
            throw new SoftwareInstallException("can't mount the dmg", e);
        }

        return new StackElement(mountDir, new CleanUpOperation(CleanUpOperation.Type.UNMOUNT_DMG, mountDir));
    }
}
