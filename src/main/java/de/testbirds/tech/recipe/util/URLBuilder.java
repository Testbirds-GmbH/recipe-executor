package de.testbirds.tech.recipe.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper singleton to build URLs to ease on-premise installations and other changes in wiring. TODO: how much should a
 * configuration file be used?
 *
 * @author testbirds
 */
public final class URLBuilder {

    /**
     * The singleton instance.
     */
    private static final URLBuilder INST = new URLBuilder();

    /**
     * A slash.
     */
    private static final String SLASH = "/";

    /**
     * The protocol for VM host accesses.
     */
    private static final String VMHOST_PROTO = "https://";

    /**
     * The local VM host (from startup) base URL.
     */
    private static final String LOCAL_VMHOST = "http://vmhost-local.testchameleon.com";

    /**
     * The software mirror base URL (without /software).
     */
    private static final String SOFTWARE_MIRROR = "https://admin.testchameleon.com/media";

    /**
     * The OS baseimage mirror base URL (with /os-images).
     */
    private static final String OS_MIRROR = "https://staging.testchameleon.com/media/os-images";

    /**
     * Domain which is used to extend VM host hostnames, i.e. hostname.domain.
     */
    private static final String VMHOST_DOMAIN = "testchameleon.com";

    /**
     * Private constructor for singleton.
     */
    private URLBuilder() {
        // empty for now...
    }

    /**
     * Get the singleton instance.
     *
     * @return the {@link URLBuilder} instance.
     */
    public static URLBuilder getInst() {
        return INST;
    }

    /**
     * Get the software mirror base URL. Please use {@link #buildSoftwareDownload(String)} method if possible instead.
     *
     * @return the base URL without terminating /.
     */
    public String getSoftwareMirror() {
        return SOFTWARE_MIRROR;
    }

    /**
     * Get the local VM host's hostname (only usable form startup).
     *
     * @return the host name of the local VM host.
     */
    public String getLocalVMHostName() {
        return LOCAL_VMHOST;
    }

    /**
     * Get the main domain, in which for example the VM hosts reside.
     *
     * @return the domain.
     */
    public String getDomain() {
        return VMHOST_DOMAIN;
    }

    /**
     * Build a VM host's FQDN based on the hostname by appending the domain.
     *
     * @param hostname the hostname of the VM host (without domain).
     * @return the FQDN.
     */
    public String buildVMHostFQDN(final String hostname) {
        return hostname + "." + VMHOST_DOMAIN;
    }

    /**
     * Build a request {@link URL} to a VM host.
     *
     * @param vmHost the VM host.
     * @param path   the relative path.
     * @return the full URL.
     * @throws MalformedURLException If the resulting URL is malformed.
     */
    public URL buildVMHost(final String vmHost, final String path) throws MalformedURLException {
        if (path.startsWith(SLASH)) {
            return new URL(VMHOST_PROTO + vmHost + path);
        } else {
            return new URL(VMHOST_PROTO + vmHost + SLASH + path);
        }
    }

    /**
     * Build a request {@link URL} to the local VM host (from startup).
     *
     * @param path the relative path.
     * @return the full URL.
     * @throws MalformedURLException If the resulting URL is malformed.
     */
    public URL buildLocalVMHost(final String path) throws MalformedURLException {
        if (path.startsWith(SLASH)) {
            return new URL(LOCAL_VMHOST + path);
        } else {
            return new URL(LOCAL_VMHOST + SLASH + path);
        }
    }

    /**
     * Build a request {@link URL} to download software from our mirror.
     *
     * @param path the relative path.
     * @return the full URL.
     * @throws MalformedURLException If the resulting URL is malformed.
     */
    public URL buildSoftwareDownload(final String path) throws MalformedURLException {
        if (path.startsWith(SLASH)) {
            return new URL(SOFTWARE_MIRROR + path);
        } else {
            return new URL(SOFTWARE_MIRROR + SLASH + path);
        }
    }

    /**
     * Build a request {@link URL} to download an OS baseimage from our mirror.
     *
     * @param path the relative path.
     * @return the full URL.
     * @throws MalformedURLException If the resulting URL is malformed.
     */
    public URL buildOSDownload(final String path) throws MalformedURLException {
        if (path.startsWith(SLASH)) {
            return new URL(OS_MIRROR + path);
        } else {
            return new URL(OS_MIRROR + SLASH + path);
        }
    }
}
