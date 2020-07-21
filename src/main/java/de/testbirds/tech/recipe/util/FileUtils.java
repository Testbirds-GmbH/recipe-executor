package de.testbirds.tech.recipe.util;

import com.google.common.base.Charsets;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.FileNameMap;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Static collection of methods which are useful for handling files.
 * <p>
 * TODO: use Path instead of File everywhere (requires refactoring of other components).
 * </p>
 *
 * @author testbirds
 */
public final class FileUtils {

    /**
     * Inode file attribute name in unix.
     */
    public static final String INODE_ATTR = "unix:ino";

    /**
     * SHA-384 string value.
     */
    public static final String SHA_384 = "SHA-384";

    /**
     * Maximum depth of a recursive method that deals directories. To try to avoid infinite loops.
     */
    public static final int MAX_DEPTH_DIRS = 3;

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    /**
     * The size in bytes of an intermediate buffer for IO transactions (should match the page size).
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * How long it should wait for the response headers in milliseconds.
     */
    private static final int HEADER_TIMEOUT = 30000;

    /**
     * The Jetty HTTP connection IDLE timeout in milliseconds.
     */
    private static final long JETTY_IDLE_TIMEOUT = 60000L;

    /**
     * Minimum amount of threads of the thread pool of Jetty client.
     */
    private static final int JETTY_MIN_THREADS = 4;

    /**
     * Maximum amount of threads of the thread pool of Jetty client.
     */
    private static final int JETTY_MAX_THREADS = 256;

    /**
     * The character set to use for all files (UTF-8).
     */
    private static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

    /**
     * Jetty http client singleton instance (lazy).
     */
    private static HttpClient jettyClient;

    /**
     * Utility class which must not be instantiated.
     */
    private FileUtils() {
    }

    /**
     * Writes a string to a file specified by its path.
     *
     * @param input the string which is written to the file
     * @param file  the output file
     * @throws IOException if an IOException occurs
     */
    public static void writeStringToFile(final String input, final File file) throws IOException {
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            out.write(input.getBytes(FILE_CHARSET));
        }
    }

    /**
     * Writes an input stream to a file specified by its path.
     *
     * @param input the input stream from which to read all its content
     * @param file  the output file
     * @throws IOException if an IOException occurs
     */
    public static void writeInputStreamToFile(final InputStream input, final File file) throws IOException {
        try (OutputStream output = Files.newOutputStream(file.toPath())) {
            final byte[] buffer = new byte[BUFFER_SIZE];
            for (int bytes = input.read(buffer); bytes > -1; bytes = input.read(buffer)) {
                output.write(buffer, 0, bytes);
            }
        } finally {
            input.close();
        }
    }

    /**
     * Lazy initialization of jetty client on demand.
     *
     * @return the {@link HttpClient} instance.
     * @throws IOException If the initialization fails.
     */
    public static HttpClient getJettyClient() throws IOException {
        synchronized (FileUtils.class) {
            if (jettyClient != null) {
                return jettyClient;
            }
            final SslContextFactory sslContextFactory = new SslContextFactory();
            jettyClient = new HttpClient(sslContextFactory);
            jettyClient.setIdleTimeout(JETTY_IDLE_TIMEOUT);
            jettyClient.setExecutor(new QueuedThreadPool(JETTY_MAX_THREADS, JETTY_MIN_THREADS));
            try {
                jettyClient.start();
            } catch (final Exception e) {
                throw new IOException("Couldn't start Jetty HttpClient.", e);
            }
            return jettyClient;
        }
    }

    /**
     * Download as stream from URL (same as URL.openStream, but with Jetty client).
     *
     * @param url the URL.
     * @return the input stream.
     * @throws IOException If an I/O exception occurs.
     */
    public static InputStream download(final URL url) throws IOException {
        final HttpClient client = getJettyClient();
        try {
            final InputStreamResponseListener listener = new InputStreamResponseListener();
            client.newRequest(url.toURI()).header(HttpHeader.CONNECTION, "close").send(listener);
            final Response response = listener.get(HEADER_TIMEOUT, TimeUnit.MILLISECONDS);
            if (HttpStatus.isSuccess(response.getStatus())) {
                return listener.getInputStream();
            } else {
                throw new IOException(
                        "Server response code: " + response.getStatus() + ". Reason: " + response.getReason());
            }
        } catch (final URISyntaxException e) {
            throw new IOException("URI syntax exception", e);
        } catch (final TimeoutException e) {
            throw new IOException("Did not receive headers after " + HEADER_TIMEOUT + "ms", e);
        } catch (final ExecutionException e) {
            throw new IOException("Error waiting for headers", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for headers to arrive", e);
        }
    }

    /**
     * Read a whole InputStream into a single string.
     *
     * @param input the InputStream to read (will be closed)
     * @return the whole content in a single string
     * @throws IOException If an I/O exception occurs
     */
    public static String readStreamToString(final InputStream input) throws IOException {
        final StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, FILE_CHARSET), BUFFER_SIZE)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append('\n');
            }
        }

        return result.toString();
    }

    /**
     * Read a whole file into a single string.
     *
     * @param file the file
     * @return the whole content in a single string
     * @throws IOException if an IOException occurs
     */
    public static String readFileToString(final File file) throws IOException {
        final StringBuilder result = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), FILE_CHARSET)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append('\n');
            }
        }

        return result.toString();
    }

    /**
     * List the content of a directory matching the globbing pattern.
     *
     * @param directory the directory
     * @param glob      the globbing pattern or empty for all files
     * @return Collection of all matching files in this directory
     * @throws IOException if an IOException occurs
     */
    public static Collection<File> listDirectory(final File directory, final String glob) throws IOException {
        final List<File> output = new ArrayList<File>();
        final DirectoryStream<Path> stream;

        if (glob.isEmpty()) {
            stream = Files.newDirectoryStream(directory.toPath());
        } else {
            stream = Files.newDirectoryStream(directory.toPath(), glob);
        }

        try {
            for (final Path entry : stream) {
                output.add(entry.toFile());
            }
        } finally {
            stream.close();
        }

        return output;
    }

    /**
     * copy a file or directory to a destination path, enforcing this as far as possible (i.e. overwrite existing target
     * files, merge directories, ...).
     *
     * @param source      the source to copy (this must exist but may be a file, symlink or directory)
     * @param destination a file object describing the target. This may exist, but is created if not and deleted if it is a file
     *                    but a directory should be copied over there
     * @throws IOException if an IOException occurs
     */
    public static void copyRecursive(final File source, final File destination) throws IOException {
        final CopyOption[] copyOptions = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES};

        if (source.isDirectory()) {
            if (destination.exists()) {
                if (!destination.isDirectory()) {
                    Files.delete(destination.toPath());
                }
            } else {
                Files.copy(source.toPath(), destination.toPath(), copyOptions);
            }

            for (final File i : listDirectory(source, "")) {
                copyRecursive(i, new File(destination.getCanonicalPath() + "/" + i.getName()));
            }
        } else {
            Files.copy(source.toPath(), destination.toPath(), copyOptions);
        }
    }

    /**
     * Delete a file or directory with recursion (potentially dangerous).
     *
     * @param file the file to remove (may also be a directory)
     * @return true if the file was successfully removed
     */
    public static boolean deleteRecursive(final File file) {
        boolean errors = false;

        if (file.isDirectory()) {
            if (file.delete()) {
                // it was a symlink!
                return true;
            }

            // ignore errors during recursion, because the final deletion will
            // fail with remaining files in a directory
            try {
                for (final File fileInside : listDirectory(file, "")) {
                    if (!deleteRecursive(fileInside)) {
                        errors = true;
                    }
                }
            } catch (final IOException e) {
                errors = true;
            }
        }

        if (!file.delete()) {
            errors = true;
        }

        return !errors;
    }

    /**
     * Delete all files from a directory which have the specified file extension.
     *
     * @param directory the directory
     * @param extension the file extension
     * @return true if all files with this extension are deleted
     */
    public static boolean deleteFilesByExtension(final File directory, final String extension) {
        boolean errors = false;

        try {
            for (final File i : listDirectory(directory, "*." + extension)) {
                if (i.isFile() && !i.delete()) {
                    errors = true;
                }
            }
        } catch (final IOException e) {
            errors = true;
        }

        return !errors;
    }

    /**
     * Extracts a zip file to the specified directory (will be created if it does not exists).
     *
     * @param zipFile         the ZIP archive file to unzip
     * @param targetDirectory the target directory where the archive is unzipped
     * @throws IOException If an I/O error occurs
     */
    public static void unzip(final File zipFile, final String targetDirectory) throws IOException {
        final File targetDir = new File(targetDirectory);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Target directory could not be created");
        }

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            // iterates over entries in the zip file
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                final String filePath = targetDir + File.separator + entry.getName();
                if (entry.isDirectory()) {
                    // if the entry is a directory, make the directory
                    final File dir = new File(filePath);
                    dir.mkdirs();
                } else {
                    new File(filePath).getParentFile().mkdirs();
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                }
                zipIn.closeEntry();
            }
        }
    }

    /**
     * Extracts a zip entry (file entry).
     *
     * @param zipIn    the ZipInputStream to read from
     * @param filePath the Path to extract
     * @throws IOException If an I/O error occurs
     */
    private static void extractFile(final ZipInputStream zipIn, final String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            final byte[] bytesIn = new byte[BUFFER_SIZE];
            for (int read = zipIn.read(bytesIn); read > 0; read = zipIn.read(bytesIn)) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    /**
     * returns the extension of the file passed as argument. detects single extensions only, will return ".gz" when
     * given "abc.tar.gz"
     *
     * @param path path to the file
     * @return extension of the file
     */

    public static String getExtension(final String path) {
        final int i = path.lastIndexOf('.');
        if (i > 0) {
            return path.substring(i + 1);
        }
        return "";
    }

    /**
     * helper method for secureCopy. Is used to check whether the remote side sent an ack.
     *
     * @param in stream
     * @return ack
     * @throws IOException error
     */
    private static int checkAck(final InputStream in) throws IOException {
        final int b = in.read();
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            final StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) {
                // error
                throw new IOException("checkAck error: " + sb.toString());
            }
            if (b == 2) {
                // fatal error
                throw new IOException("checkAck fatal error: " + sb.toString());
            }
        }
        return b;
    }

    /**
     * Create a directory and its parent directories if they do not exist.
     *
     * @param directoryString Directory path
     * @return Path pointing to created directory
     * @throws IOException When creating directory fails
     */
    public static Path createDirectory(final String directoryString) throws IOException {
        final Path directoryPath = Paths.get(directoryString);
        final Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        final FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
        Files.createDirectories(directoryPath, fileAttributes);
        LOG.info("{} exists {}", directoryString, directoryPath.toFile().exists());
        LOG.info("{} is directory {}", directoryString, directoryPath.toFile().isDirectory());
        return directoryPath;
    }

    /**
     * Gets a list with all file from input directory and subdirectories.
     *
     * @param directory Directory to read
     * @param maxDepth  Maximum directory depth it will list files from
     * @return Collection with files found in directory and subdirectories
     * @throws IOException If getting list of files fails
     */
    public static List<File> listDirectoryAndSubdirectories(final Path directory, final int maxDepth)
            throws IOException {
        if (maxDepth > MAX_DEPTH_DIRS) {
            throw new IOException("Maximum depth specified exceeds maximum allowed.");
        } else if (maxDepth < 1) {
            return Collections.emptyList();
        }

        final List<File> files = new ArrayList<>();
        final Collection<File> folders = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (final Path path : directoryStream) {
                final File file = path.toFile();
                if (file.isFile()) {
                    files.add(file);
                } else {
                    folders.add(file);
                }
            }
        }

        // List files folders outside the try/catch to avoid opening more DirectoryStreams without closing the one above
        for (final File folder : folders) {
            files.addAll(listDirectoryAndSubdirectories(folder.toPath(), maxDepth - 1));
        }
        return files;
    }

    /**
     * Loads a property file from the file system.
     *
     * @param propertyFile The file it should read from.
     * @return The {@link Properties}
     * @throws IOException If the file does not exist or it can't read the file
     */
    public static Properties loadPropertyFile(final File propertyFile) throws IOException {
        try (InputStream input = Files.newInputStream(propertyFile.toPath())) {
            final Properties properties = new Properties();
            properties.load(new BufferedReader(new InputStreamReader(input, Charsets.UTF_8)));
            return properties;
        }
    }

    /**
     * Get the MIME type of a file or the default value specified.
     *
     * @param file         The file
     * @param defaultValue Default value in case MIME cannot be determined
     * @return MIME type of the file
     */
    public static String getMimeType(final File file, final String defaultValue) {
        final FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mime = fileNameMap.getContentTypeFor(file.getAbsolutePath());
        if (mime == null) {
            try {
                mime = Files.probeContentType(file.toPath());
            } catch (final IOException e) {
                LOG.warn("Failed to detect mime of " + file.getAbsolutePath(), e);
            }
        }
        if (mime == null) {
            return defaultValue;
        }

        return mime;
    }

    /**
     * Creates a new file allocating all the space in disk it will have.
     *
     * @param localPath    Path where file will be created
     * @param fileSize     Size to allocate
     * @param minBytesFree Minimum remaining amount of bytes the file system will have to have free after allocating the space
     * @return File created with all space allocated
     * @throws IOException if error occurs
     */
    public static File allocateSpace(final String localPath, final long fileSize, final long minBytesFree)
            throws IOException {
        // Check if there is enough space
        final long usableSpace = new File(localPath).getUsableSpace();
        if (usableSpace <= fileSize + minBytesFree) {
            throw new IOException("Not enough disk space available to allocate file with size " + fileSize);
        }

        final Path tempFolder = Paths.get(localPath);
        final File tempFile = Files.createTempFile(tempFolder, "", "").toFile();

        try (FileChannel fileChannel = FileChannel.open(tempFile.toPath(), StandardOpenOption.WRITE)) {
            fileChannel.position(fileSize - 1);
            fileChannel.write(ByteBuffer.allocate(1));
        }

        return tempFile;
    }

    /**
     * Get the inode of a file.
     *
     * @param file File to get the inode from
     * @return The inode of the file
     * @throws IOException if the inode cannot be determined
     */
    public static Long getInode(final File file) throws IOException {
        return getInode(file.toPath());
    }

    /**
     * Get the inode of a file path.
     *
     * @param path File path to get the inode from
     * @return The inode of the file represented by the path
     * @throws IOException if the inode cannot be determined
     */
    public static Long getInode(final Path path) throws IOException {
        return (Long) Files.getAttribute(path, INODE_ATTR);
    }

    /**
     * Get all local mount points.
     *
     * @return All the local mount points
     */
    public static Set<String> getMountPoints() {
        final Iterable<FileStore> fileStores = FileSystems.getDefault().getFileStores();
        return StreamSupport.stream(fileStores.spliterator(), false)
                .map(fileStore -> fileStore.toString().split(" ")[0]).collect(Collectors.toSet());
    }

    /**
     * Determine the mount point of a file.
     *
     * @param file The file
     * @return the mount point where the file is
     * @throws IOException if mount point of the file cannot be determined
     */
    public static String getMountPointOfFile(final File file) throws IOException {
        final Set<String> mountPoints = getMountPoints();
        File currentFile = file;
        while (currentFile != null && currentFile.exists()) {
            for (final String mountPoint : mountPoints) {
                if (mountPoint.equals(currentFile.getCanonicalPath())) {
                    return mountPoint;
                }
            }
            currentFile = currentFile.getParentFile();
        }
        throw new IOException("Could not find mount point of file " + file);
    }
}
