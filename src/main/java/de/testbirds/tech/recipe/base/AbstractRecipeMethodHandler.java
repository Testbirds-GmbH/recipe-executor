package de.testbirds.tech.recipe.base;

import de.testbirds.tech.recipe.report.SoftwareInstallException;

/**
 * extract this class to handle a RecipeStep with a specific RecipeMethod.
 */
public abstract class AbstractRecipeMethodHandler {
    /**
     * will be called with the one resolved parameter and additionally some other arguments from the stack if told so by
     * requiresAdditionalArguments().
     *
     * @param parameter the resolved parameter that was given to this step
     * @param callback  the de.testbirds.tech.recipe.base.InstallerCallback that allows to execute some special actions.
     * @return a stack element that is pushed to the stack
     * @throws SoftwareInstallException executing this step failed
     */
    public abstract StackElement handle(String parameter, Installer callback) throws SoftwareInstallException;
}
