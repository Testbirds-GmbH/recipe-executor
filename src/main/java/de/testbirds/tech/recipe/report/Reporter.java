package de.testbirds.tech.recipe.report;

import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.entity.RecipeStep;

/**
 * is used to get some reports about the installation.
 */
public interface Reporter {

    /**
     * report the execution of this step and result.
     *
     * @param step   the executed step
     * @param result the result
     */
    void report(RecipeStep step, StackElement result);

}
