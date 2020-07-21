package de.testbirds.tech.recipe.base;

import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.FileUtils;
import de.testbirds.tech.recipe.util.ProcessRunner;

import java.io.File;
import java.io.IOException;

/**
 * clean up some stuff after it isn't needed any more.
 */
public final class CleanUpOperation {
    /**
     * type of this clean up operation.
     */
    private final Type type;
    /**
     * the resource that is cleaned up.
     */
    private final String resource;

    /**
     * clean up a specific resource.
     *
     * @param type     type of undo operation
     * @param resource the resource that is cleaned up
     */
    public CleanUpOperation(final Type type, final String resource) {
        this.type = type;
        this.resource = resource;
    }

    /**
     * getter.
     *
     * @return type
     */
    public Type getType() {
        return type;
    }

    /**
     * getter.
     *
     * @return resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * execute this clean up operation.
     *
     * @throws SoftwareInstallException there was an error
     */
    public void cleanUp() throws SoftwareInstallException {
        switch (type) {
            case UNMOUNT_DMG:
                try {
                    ProcessRunner.runCommand(new String[]{"hdiutil", "unmount", resource}, null);
                } catch (final IOException | InterruptedException e) {
                    throw new SoftwareInstallException("can't unmount the dmg", e);
                }
                break;
            case DELETE:
                FileUtils.deleteRecursive(new File(resource));
                break;
            default:
                throw new IllegalArgumentException("unknown clean up operation: " + type);
        }
    }

    /**
     * the type of the de.testbirds.tech.recipe.base.CleanUpOperation.
     */
    public enum Type {
        /**
         * unmount a dmg.
         */
        UNMOUNT_DMG,
        /**
         * delete a file or folder.
         */
        DELETE
    }

}
