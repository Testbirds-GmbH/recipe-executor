package de.testbirds.tech.recipe;

import de.testbirds.tech.recipe.entity.Arch;
import de.testbirds.tech.recipe.entity.OSFamily;

import java.util.UUID;

/**
 * The startup is responsible for the interaction with the underlying OS.
 */
public interface Startup {

    /**
     * Get the unique ID of this VM.
     */
    UUID getUUID();

    /**
     * Check which OS family we're running on.
     */
    OSFamily determineOSFamily();

    /**
     * Determine the architecture of this VM.
     */
    Arch determineOSArch();

    /**
     * Determine the os version of this VM.
     */
    String determineOSVersion();

    /**
     * Reboot the VM.
     */
    void reboot();
}
