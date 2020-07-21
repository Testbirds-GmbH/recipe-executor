package de.testbirds.tech.recipe.base;

/**
 * represents a element on the stack. contains a string, and maybe stdout + stdin (not necessary)
 */
public final class StackElement {
    /**
     * undo this steps after POPing this element.
     */
    private final CleanUpOperation cleanUp;

    /**
     * stdOut.
     */
    private final String stdOut;

    /**
     * stdErr.
     */
    private final String stdErr;

    /**
     * element.
     */
    private final String elem;

    /**
     * create a new stack element with only a string.
     *
     * @param str the string this stack element represents
     */
    public StackElement(final String str) {
        this(str, null);
    }

    /**
     * create new stack element with string and clean up operation.
     *
     * @param str     the string
     * @param cleanUp the clean up operation
     */
    public StackElement(final String str, final CleanUpOperation cleanUp) {
        this(str, cleanUp, null, null);
    }

    /**
     * default constructor that is used by jackson. Everything can be null except the elem.
     *
     * @param elem    content of this element
     * @param cleanUp clean up operation
     * @param stdOut  std out
     * @param stdErr  std err
     */
    public StackElement(final String elem,
                        final CleanUpOperation cleanUp, final String stdOut,
                        final String stdErr) {
        this.elem = elem;
        this.cleanUp = cleanUp;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    /**
     * getter.
     *
     * @return stdOut
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * getter.
     *
     * @return stdErr
     */
    public String getStdErr() {
        return stdErr;
    }

    /**
     * getter.
     *
     * @return elem
     */
    public String getElem() {
        return elem;
    }

    /**
     * getter.
     *
     * @return clean up operation
     */
    public CleanUpOperation getCleanUp() {
        return cleanUp;
    }

    @Override
    public String toString() {
        return elem;
    }

    /**
     * create a nice representation of this stack element.
     *
     * @return string representation
     */
    public String prettyPrint() {
        final StringBuilder msg = new StringBuilder(30);
        msg.append(elem);
        if (stdOut != null || stdErr != null) {
            msg.append(" [").append(stdOut).append('|').append(stdErr).append(']');
        }
        return msg.toString();
    }
}
