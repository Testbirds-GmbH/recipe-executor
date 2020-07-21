package de.testbirds.tech.recipe.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static collection of methods to run a process or script.
 *
 * @author testbirds
 */
public final class ProcessRunner {

    /**
     * SLF4J logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessRunner.class);

    /**
     * The signals to use for killing a process in order.
     */
    private static final String[] KILLING_SIGNALS = new String[]{"TERM", "ABRT", "KILL"};

    /**
     * The time to wait for the process to exit cleanly before the next (stronger) signal is used.
     */
    private static final long KILLING_GRACE_TIME = 2000L;

    /**
     * Utility class which must not be instantiated.
     */
    private ProcessRunner() {
    }

    /**
     * Output a shell executable escaped path to the file.
     *
     * @param file the file.
     * @return the path string which can be used in shell commands.
     */
    public static String escapePathForShell(final File file) {
        return '\'' + file.getAbsolutePath().replaceAll("'", "'\"'\"'") + '\'';
    }

    /**
     * Start a process and return it.
     *
     * @param cmdLine the command line.
     * @param input   input written to the process (may be null for no STDIN).
     * @return the new launched process.
     * @throws IOException If there is an exception during process creation or input writing.
     */
    public static Process startProcess(final String[] cmdLine, final String[] input) throws IOException {
        return startProcess(cmdLine, input, null);
    }

    /**
     * Start a process with environmental variables and return it.
     *
     * @param cmdLine the command line.
     * @param input   input written to the process (may be null for no STDIN).
     * @param envp    an array of environmental variables (e.g. 'DISPLAY=2')
     * @return the new launched process.
     * @throws IOException If there is an exception during process creation or input writing.
     */
    public static Process startProcess(final String[] cmdLine, final String[] input, final String[] envp)
            throws IOException {
        final Process process = Runtime.getRuntime().exec(cmdLine, envp);
        if (input != null) {
            writeInputLines(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), input);
        }
        return process;
    }

    /**
     * The most basic way to run a process synchronously with IO control.
     *
     * @param cmdLine the command to run (cmdLine[0] command, cmdLine[i] argument)
     * @param input   pass input to the process, line by line
     * @param output  capture stdout to output[0] and optionally stderr to output[1]
     * @return exit code of the process
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static int runProcess(final String[] cmdLine, final String[] input, final String[] output)
            throws IOException, InterruptedException {
        return runProcess(cmdLine, input, output, null);
    }

    /**
     * The most basic way to run a process synchronously with IO control.
     *
     * @param cmdLine the command to run (cmdLine[0] command, cmdLine[i] argument)
     * @param input   pass input to the process, line by line
     * @param output  capture stdout to output[0] and optionally stderr to output[1]
     * @param envp    an array of environmental variables
     * @return exit code of the process
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static int runProcess(final String[] cmdLine, final String[] input, final String[] output,
                                 final String[] envp) throws IOException, InterruptedException {
        final Process process = startProcess(cmdLine, input, envp);

        // wait for the termination of the process
        final int exitCode = process.waitFor();

        // capture the output of the process
        if (output != null && output.length > 0) {
            output[0] = captureInputStream(process.getInputStream());
            if (output.length >= 2) {
                output[1] = captureInputStream(process.getErrorStream());
            }
        }

        return exitCode;
    }

    /**
     * Run a single command and treat nonzero exit codes as an error. This method throws an IOException with the exit
     * code and the error output of the process if its exit code is nonzero.
     *
     * @param cmdLine the command to run (cmdLine[0] command, cmdLine[i] argument)
     * @param input   pass input to the command, line by line
     * @return standard output of the command
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static String runCommand(final String[] cmdLine, final String[] input)
            throws IOException, InterruptedException {
        final String[] output = new String[2];

        final int exitCode = runProcess(cmdLine, input, output);
        if (exitCode != 0) {
            throw new IOException(cmdLine[0] + " exited with error code " + exitCode + ": " + output[0] + output[1]);
        }

        return output[0];
    }

    /**
     * write the script lines to a .bat script.
     *
     * @param scriptLines the script that will be written
     * @return the bat file
     * @throws IOException read write error
     */
    public static File writeBatchScript(final String[] scriptLines) throws IOException {
        final File tmpFile = File.createTempFile("tech", ".bat");
        final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.ISO_8859_1));
        try {
            for (int i = 0; i < scriptLines.length; i++) {
                writer.write(scriptLines[i]);
                writer.newLine();
            }
        } finally {
            writer.close();
        }

        return tmpFile;
    }

    /**
     * Execute a script in the BATCH shell (Windows).
     *
     * @param scriptLines the script line by line (line separators are added)
     * @param output      like in runProcess, this allows to capture the script's output
     * @return exit code of the script
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static int runBatchScript(final String[] scriptLines, final String[] output)
            throws IOException, InterruptedException {
        final File tmpFile = writeBatchScript(scriptLines);

        // run the batch file without input (BATCH)
        final int exitCode = runProcess(new String[]{tmpFile.getAbsolutePath()}, null, output);

        Files.delete(tmpFile.toPath());

        return exitCode;
    }

    /**
     * Execute a script in the BASH shell (Linux and Mac).
     *
     * @param scriptLines the script line by line (line seperators are added)
     * @param output      like in runProcess, this allows to capture the script's output
     * @return exit code of the script
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static int runBashScript(final String[] scriptLines, final String[] output)
            throws IOException, InterruptedException {
        return runProcess(new String[]{"bash", "-s", "-e"}, scriptLines, output);
    }

    /**
     * Execute a script in AppleScript (Mac).
     *
     * @param scriptLines the script line by line (line seperators are added)
     * @param output      like in runProcess, this allows to capture the script's output
     * @return exit code of the script
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static int runAppleScript(final String[] scriptLines, final String[] output)
            throws IOException, InterruptedException {
        return runProcess(new String[]{"osascript"}, scriptLines, output);
    }

    /**
     * Kill a process by its PID on Unixoid systems like Linux or Mac OS X. It will kill with different signals,
     * starting with SIGTERM, escalating to SIGABRT and finally SIGKILL if the process does not terminate properly.
     *
     * @param pid the PID number to kill.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting for the process to
     *                              terminate.
     */
    public static void killProcess(final int pid) throws IOException, InterruptedException {
        for (final String signal : KILLING_SIGNALS) {

            // check if the process is still running
            try {
                final String output = ProcessRunner.runCommand(new String[]{"ps", "-p", Integer.toString(pid)},
                        null);
                if (output.length() > 0) {
                    LOG.debug("ps -p {} output: {}", pid, output);
                }
            } catch (final IOException e) {
                // this means, ps failed because the process does not exist
                return;
            }

            // kill the process with the specified signal
            try {
                final String output = ProcessRunner
                        .runCommand(new String[]{"kill", "-" + signal, Integer.toString(pid)}, null);
                if (output.length() > 0) {
                    LOG.warn("Kill -{} output: {}", signal, output);
                }
            } catch (final IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Killing the process with -" + signal + " failed:", e);
                }
                continue;
            }

            // give the process some time to terminate
            Thread.sleep(KILLING_GRACE_TIME);
        }
        LOG.error("Failed to kill the process with PID {}.", pid);
    }

    /**
     * Stop a daemon with the specified PID file, forcing the daemon to quit and also removing the PID file (requires
     * root access).
     *
     * @param pidFile the PID file
     * @throws FileNotFoundException If the PID file is not found.
     * @throws IOException           If an I/O error occurs.
     * @throws InterruptedException  If the current thread is interrupted by another thread while it is waiting for the daemon to
     *                               terminate.
     */
    public static void stopDaemon(final File pidFile) throws FileNotFoundException, IOException, InterruptedException {
        if (!pidFile.isFile()) {
            throw new FileNotFoundException("PID file " + pidFile.getAbsolutePath() + " does not exist.");
        }
        try {
            final int pid;
            try {
                pid = Integer.parseInt(FileUtils.readFileToString(pidFile).trim());
            } catch (final NumberFormatException e) {
                throw new IOException("PID file contained an invalid PID: " + pidFile.getAbsolutePath(), e);
            }
            killProcess(pid);
        } finally {
            if (!pidFile.delete()) {
                LOG.warn("PID file was already deleted.");
            }
        }
    }

    /**
     * Start a daemon directly without a service file / init script (requires root access).
     *
     * @param executable the path of the executable which is started as daemon
     * @param parameters the command line parameters of the daemon's process
     * @param pidFile    a PID file which will be created with the process' PID for stopping the daemon
     * @param user       optionally a user to switch to
     * @param group      optionally a group to switch to
     * @param daemonize  if this is true, the process will be daemonized (use this if the process stays in foreground)
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static void startDaemon(final String executable, final String[] parameters, final String pidFile,
                                   final String user, final String group, final boolean daemonize) throws IOException, InterruptedException {
        final List<String> fullCMD = new ArrayList<String>(
                Arrays.asList("start-stop-daemon", "--start", "--exec", executable));

        // add pidfile, if used
        if (pidFile != null && !pidFile.isEmpty()) {
            fullCMD.add("--pidfile");
            fullCMD.add(pidFile);
        }

        // add user and group switching, if requested
        if (user != null && !user.isEmpty()) {
            fullCMD.add("--user");
            fullCMD.add(user);
        }
        if (group != null && !group.isEmpty()) {
            fullCMD.add("--group");
            fullCMD.add(group);
        }

        // if daemonize is set, daemonize the command directly
        if (daemonize) {
            fullCMD.add("--background");
            if (pidFile != null && !pidFile.isEmpty()) {
                fullCMD.add("--make-pidfile");
            }
        }

        // add parameters
        if (parameters != null && parameters.length > 0) {
            fullCMD.add("--");
            fullCMD.addAll(Arrays.asList(parameters));
        }

        // run the full command
        runCommand(fullCMD.toArray(new String[fullCMD.size()]), null);
    }

    /**
     * Stop all processes (usually a daemon with subprocesses) which use a TCP port (requires root access).
     *
     * @param port the TCP port number
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static void stopDaemonByTCPPort(final int port) throws IOException, InterruptedException {
        runCommand(new String[]{"fuser", "-k", "-TERM", "-n", "tcp", Integer.toString(port)}, null);
    }

    /**
     * Execute a single shell command on a dedicated graphical user terminal. This hack does not allow to capture IO of
     * the process, as it is printed on the terminal. Additionally, it is not allowed to put " or ' into the command, as
     * this may break the internal implementation. Currently, this hack is only needed for Selenium.
     *
     * @param cmdLine the command line which is run. May not contain " or '
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting
     */
    public static void runProcessInTerminal(final String cmdLine) throws IOException, InterruptedException {
        String[] script;
        final String osName = System.getProperty("os.name").toLowerCase();

        if (osName.indexOf("windows") >= 0) {

            // Windows hack: use START command in a batch file
            script = new String[]{"START /min /dC:\\ " + cmdLine};

            // TODO: do some checks if the terminal actually opened.
            runBatchScript(script, null);
        } else if (osName.indexOf("linux") >= 0) {

            // Linux hack: open xterm on DISPLAY :0.0 (this hack only works on
            // systems with a user "testbirds" and gdm3 as the login manager)
            script = new String[]{". /etc/profile",
                    "export XAUTHORITY=\"$( find /var/run/gdm3 -name auth-for-testbirds-* | head -n1 )/database\"",
                    "export DISPLAY=\":0.0\"", "su testbirds -c 'xterm -iconic -e \"" + cmdLine + "\"'"};

            // TODO: do some checks if the terminal actually opened.
            final int exitCode = runBashScript(script, null);
            if (exitCode != 0) {
                throw new IOException("Opening the terminal failed with exit code " + exitCode);
            }
        } else if (osName.indexOf("mac") >= 0) {

            // Mac hack: open app "Terminal" and execute the command
            script = new String[]{"tell app \"Terminal\"", "do script \"" + cmdLine + "\"", "end tell"};

            // TODO: do some checks if the terminal actually opened.
            runAppleScript(script, null);
        } else {
            throw new IOException("Unsupported operating system for opening a terminal: " + osName);
        }
    }

    /**
     * Write a string array line by line to an output stream.
     *
     * @param output     the output stream to which the input is written
     * @param inputLines contains the input line by line (line seperators are added)
     * @throws IOException If an I/O error occurs
     */
    private static void writeInputLines(final Writer output, final String[] inputLines) throws IOException {
        final BufferedWriter writer = new BufferedWriter(output);

        for (int i = 0; i < inputLines.length; i++) {
            writer.write(inputLines[i]);
            writer.newLine();
        }

        writer.close();
    }

    /**
     * Capture input from an input stream line by line and aggregate it.
     *
     * @param input the input stream which is read
     * @return String of all lines always seperated by \n
     * @throws IOException If an I/O error occurs
     */
    private static String captureInputStream(final InputStream input) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        final StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }

        return stringBuilder.toString();
    }
}
