package de.testbirds.tech.recipe.report;

import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.entity.RecipeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * log the execution locally to the log file.
 */
public class LocalReporter implements Reporter {

    /**
     * the logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalReporter.class);

    /**
     * used to identify different reporter.
     */
    private final String tag;

    /**
     * new local reporter.
     *
     * @param tag the tag to identify different reporter.
     */
    public LocalReporter(final String tag) {
        this.tag = tag + ": ";
    }

    /**
     * new local reporter.
     */
    public LocalReporter() {
        this.tag = "";
    }

    @Override
    public final void report(final RecipeStep step, final StackElement result) {
        final String msg = tag + "executed " + step.getMethod() + " with result " + result.prettyPrint();
        LOGGER.debug(msg);
    }

}
