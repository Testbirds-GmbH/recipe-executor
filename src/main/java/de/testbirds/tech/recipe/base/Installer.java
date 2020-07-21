package de.testbirds.tech.recipe.base;

import de.testbirds.tech.recipe.Startup;
import de.testbirds.tech.recipe.entity.RecipeStep;
import de.testbirds.tech.recipe.entity.SoftwareVersion;
import de.testbirds.tech.recipe.report.Reporter;
import de.testbirds.tech.recipe.report.SoftwareInstallException;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * a callback that is passed to the handlers. it allows to get additional elements from the stack
 */
public interface Installer {

    /**
     * get one additional parameter from the stack.
     *
     * @return the second top most element on the stack
     */
    String getAdditionalParameter();

    /**
     * get additional parameters from the stack.
     *
     * @param number the amount of parameters you want to
     * @return an array with n elements. The smaller the index in the array, the higher the element was on the stack
     */
    String[] getAdditionalParameters(int number);

    /**
     * get all children of the last executed recipe step. use this with caution, because the steps afterwards are not
     * executed from the de.testbirds.tech.recipe.base.Installer any more. That is the job of the caller of this method
     *
     * @return all recipesteps that would be executed after the current step and that are deeper in the hierarchy than
     * the current one
     */
    Queue<RecipeStep> getChildren();

    /**
     * returns a copy of the current stack.
     *
     * @return stack
     */
    Stack<StackElement> getStack();

    /**
     * gives back the startup.
     *
     * @return the startup this installer was started from.
     */
    Startup getStartup();

    /**
     * Execute these steps in the same context as the original step. Make sure the additional steps don't mess around
     * with the stack (amount of POPs, leave the original part of the stack as it is!). Otherwise unintended behavior
     * might happen.
     *
     * @param steps to execute
     * @throws SoftwareInstallException something went wrong while installing
     */
    void insert(List<RecipeStep> steps) throws SoftwareInstallException;

    /**
     * getter for the current reporter (helpfull when child steps want to report to their parent reporter).
     *
     * @return reporter of this installer
     */
    Reporter getReporter();

    /**
     * Getter for software being installed.
     *
     * @return software
     */
    SoftwareVersion getSoftware();
}
