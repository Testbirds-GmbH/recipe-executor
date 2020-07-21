package de.testbirds.tech.recipe;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.RecipeMethodDispatcher;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.entity.RecipeMethod;
import de.testbirds.tech.recipe.entity.RecipeStep;
import de.testbirds.tech.recipe.entity.SoftwareVersion;
import de.testbirds.tech.recipe.report.InvalidRecipeException;
import de.testbirds.tech.recipe.report.LocalReporter;
import de.testbirds.tech.recipe.report.Reporter;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the new recipe2 installer.
 */
public class RecipeInstaller implements Installer {

    /**
     * the logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeInstaller.class);

    /**
     * std out variable name.
     */
    private static final String STD_OUT = "STD_OUT";

    /**
     * std err variable name.
     */
    private static final String STD_ERR = "STD_ERR";

    /**
     * number of local resolved variables. STD_OUT, STD_ERR and numbers
     */
    private static final String[] PATTERNS = new String[]{"\\d+", STD_OUT, STD_ERR};

    /**
     * regex pattern to replace parameters in strings.
     */
    private static Pattern pattern;

    /**
     * handlers for the recipe steps.
     */
    private final RecipeMethodDispatcher recipeMethodDispatcher;

    /**
     * the stack.
     */
    private final Stack<StackElement> stack;

    /**
     * the recipe2 steps. they are executed from top to bottom.
     */
    private final Queue<RecipeStep> steps;

    /**
     * the callback all steps are reported to.
     */
    private final Reporter reporter;

    private final SoftwareVersion software;
    /**
     * the constant resolver.
     */
    private final ConstantResolver constantResolver;
    /**
     * the startup.
     */
    private Startup startup;

    public RecipeInstaller(final Startup startup, final SoftwareVersion software) throws InvalidRecipeException {
        this(startup, software, new LocalReporter(), RecipeMethodDispatcher.getInst(), new LinkedList<>(software.getSteps()), new Stack<>());
        this.stack.push(new StackElement(resolve(software.getParameter())));
    }

    /**
     * Installer for software.
     *
     * @param startup                the startup
     * @param software               the software
     * @param reporter               the callback. all steps are reported
     * @param recipeMethodDispatcher dispatcher handlers for recipe steps
     * @param recipe                 the list of recipe steps
     * @param stack                  the starting stack
     */
    public RecipeInstaller(final Startup startup, final SoftwareVersion software, final Reporter reporter,
                           final RecipeMethodDispatcher recipeMethodDispatcher, final Queue<RecipeStep> recipe,
                           final Stack<StackElement> stack) {
        this.startup = startup;
        this.software = software;
        this.recipeMethodDispatcher = recipeMethodDispatcher;
        this.stack = stack;
        this.steps = recipe;
        this.reporter = reporter;
        this.constantResolver = new ConstantResolver(startup);
    }

    /**
     * compile the pattern that is used to resolve the step parameters.
     */
    private static void compilePattern() {
        final String[] subpatterns = new String[PATTERNS.length + 1];
        System.arraycopy(PATTERNS, 0, subpatterns, 0, PATTERNS.length);
        subpatterns[PATTERNS.length] = ConstantResolver.getRegexPattern();
        // compile the pattern only once
        final StringBuilder patternSb = new StringBuilder();
        patternSb.append("\\{\\{(");
        for (int i = 0; i < subpatterns.length; i++) {
            patternSb.append(subpatterns[i]);
            if (i != subpatterns.length - 1) {
                patternSb.append('|');
            }
        }
        patternSb.append(")\\}\\}");
        LOGGER.debug("pattern: {}", patternSb);
        pattern = Pattern.compile(patternSb.toString());
    }

    /**
     * does the installation of a Recipe2.
     *
     * @throws SoftwareInstallException installing this sw failed
     */
    public final void execute() throws SoftwareInstallException {
        LOGGER.debug("RecipeInstaller start installing");
        while (!steps.isEmpty()) {
            final RecipeStep step = steps.poll();
            executeStep(step);
        }
        LOGGER.debug("RecipeInstaller finished installing");
        cleanUpStack();
    }

    /**
     * execute a single step.
     *
     * @param step the single step
     * @throws SoftwareInstallException installing failed
     */
    private void executeStep(final RecipeStep step) throws SoftwareInstallException {
        LOGGER.debug("Executing {}.", step);
        if (step.getMethod() == RecipeMethod.POP) {
            // handle a POP
            final StackElement top = stack.pop();
            if (top.getCleanUp() != null) {
                top.getCleanUp().cleanUp();
            }
        } else {
            final AbstractRecipeMethodHandler handler = recipeMethodDispatcher.getHandler(step.getMethod());
            final String resolved = resolve(step.getParameter());
            final StackElement result = handler.handle(resolved, this);
            reporter.report(step, result);
            stack.push(result);
        }
    }

    @Override
    public final String getAdditionalParameter() {
        return stack.get(stack.size() - 1).getElem();
    }

    @Override
    public final String[] getAdditionalParameters(final int number) {
        final String[] additionalArguments = new String[number];
        for (int i = 0; i < number; i++) {
            additionalArguments[i] = stack.get(stack.size() - 1 - i).getElem();
        }
        return additionalArguments;
    }

    @Override
    public final Stack<StackElement> getStack() {
        final Stack<StackElement> newStack = new Stack<>();
        newStack.addAll(stack);
        return newStack;
    }

    @Override
    public final Queue<RecipeStep> getChildren() {
        final LinkedList<RecipeStep> children = new LinkedList<>();
        int level = 0;
        while (level >= 0) {
            if (level == 0 && steps.peek().getMethod() == RecipeMethod.POP) {
                // we reached the step that closes the list of children
                // it is important, that this element is not removed
                level--;
            } else {
                final RecipeStep step = steps.poll();
                children.add(step);
                if (step.getMethod() == RecipeMethod.POP) {
                    level--;
                } else {
                    level++;
                }
            }
        }
        return children;
    }

    /**
     * clean up the whole given stack. is equal to executing n=stack.size() times POP
     *
     * @throws SoftwareInstallException error while undoing some step
     */
    public final void cleanUpStack() throws SoftwareInstallException {
        // clean up afterwards. Usually the last POP should not left any undo operations, but for internal use cases it
        // may be necessary
        while (!stack.isEmpty()) {
            executeStep(new RecipeStep(RecipeMethod.POP, ""));
        }
    }

    /**
     * replaces variables inside the parameter string. (for example: {{0}}, {{STD_OUT}}, ...)
     *
     * @param parameter the parameter string before
     * @return parameter string with variables replaced
     * @throws InvalidRecipeException there was an error related to how the recipe is defined.
     */
    public final String resolve(final String parameter) throws InvalidRecipeException {
        LOGGER.debug("resolve: {} with stack: {}", parameter, stack);
        if (pattern == null) {
            compilePattern();
        }
        final Matcher matcher = pattern.matcher(parameter);

        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String group = matcher.group(1);
            String replacement = null;
            switch (group) {
                case STD_OUT:
                    replacement = stack.peek().getStdOut();
                    if (replacement == null) {
                        replacement = "";
                    }
                    break;
                case STD_ERR:
                    replacement = stack.peek().getStdErr();
                    if (replacement == null) {
                        replacement = "";
                    }
                    break;
                default:
                    try {
                        final int index = Integer.parseInt(matcher.group(1));
                        final int stackIndex = stack.size() - index - 1;
                        // content is a number, so it refers to an element on the stack
                        if (stackIndex < 0) {
                            throw new InvalidRecipeException(
                                    "Requested stack elem " + index + " but stack has size " + stack.size());
                        }
                        replacement = stack.get(stackIndex).getElem();
                    } catch (final NumberFormatException nfe) {
                        // no number, it may be a special constant. let the Startup resolve this constant if possible:
                        replacement = constantResolver.resolve(matcher.group(1));
                    }
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        LOGGER.debug("resolved: {}", sb.toString());
        return sb.toString();
    }

    @Override
    public final Startup getStartup() {
        return startup;
    }

    @Override
    public final void insert(final List<RecipeStep> theseSteps) throws SoftwareInstallException {
        LOGGER.debug("Will insert these steps into the normal pipeline: {}", theseSteps);
        for (final RecipeStep step : theseSteps) {
            executeStep(step);
        }
    }

    @Override
    public final Reporter getReporter() {
        return reporter;
    }

    @Override
    public final SoftwareVersion getSoftware() {
        return software;
    }

}
