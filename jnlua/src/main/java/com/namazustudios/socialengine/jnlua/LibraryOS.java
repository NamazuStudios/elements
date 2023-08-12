package com.namazustudios.socialengine.jnlua;

import static java.lang.String.format;

/**
 * Provides some constants related to library locations on the classpath.
 */
public enum LibraryOS {

    UNSPECIFIED("") {
        @Override
        public String getClasspathPrefix() {
            final String osName = System.getProperty("os.name");
            throw new IllegalStateException("Unknown OS: " + osName);
        }
    },
    MAC("darwin") {
        @Override
        public String getLibraryBaseClasspath() {
            return "darwin";
        }
    },
    LINUX("linux");

    private final String classpathPrefix;

    LibraryOS(final String classpathPrefix) {
        this.classpathPrefix = classpathPrefix;
    }

    /**
     * The file extension for hte library (eg dll, dylib, or so).
     *
     * @return the file extension
     */
    public String getClasspathPrefix() {
        return classpathPrefix;
    }

    /**
     * Gets the canonical architecture of the system.  This sorts out the specific architecture based on system
     * properties.
     * @return the canonical architecture
     */
    public String getCanonicalArchitecture() {

        final String arch = System.getProperty("os.arch");

        if ("powerpc".equals(arch)) {
            return "ppc";
        } else if ("powerpc64".equals(arch)) {
            return "ppc64";
        } else if ("i386".equals(arch) || "i686".equals(arch)) {
            return "x86";
        } else if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            return "x86-64";
        } else if ("ppc64".equals(arch) && "little".equals(System.getProperty("sun.cpu.endian"))) {
            return "ppc64le";
        } else if ("aarch64".equals(arch)) {
            return "aarch64";
        }else {
            throw new IllegalStateException("Unable to determine architecture.");
        }

    }

    /**
     * Gets the base classpath for the libraries.
     *
     * @return the base classpath for the libraries
     */
    public String getLibraryBaseClasspath() {
        return format("%s-%s", getClasspathPrefix(), getCanonicalArchitecture());
    }

    /**
     * Gets just the filename of the library with the supplied name.
     *
     * @param libraryName the library file name
     * @return the library file name, not associated with a path
     */
    public String getLibraryFilename(final String libraryName) {
        return System.mapLibraryName(libraryName);
    }

    /**
     * Gets the full classpath for the specified library.
     *
     * @param libraryName the library
     * @return the full classpath for the named library
     */
    public String getLibraryFullClasspath(final String libraryName) {
        return format("/%s/%s", getLibraryBaseClasspath(), getLibraryFilename(libraryName));
    }

}
