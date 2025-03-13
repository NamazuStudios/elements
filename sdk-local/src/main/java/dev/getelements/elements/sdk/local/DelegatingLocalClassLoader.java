package dev.getelements.elements.sdk.local;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.local.SystemClasspathUtils.getSystemClasspath;

/**
 * Delegates to the {@link #getSystemClassLoader()} using it for all loading and reads the system classpath for
 * locating all resources.
 */
public class DelegatingLocalClassLoader extends ClassLoader {

    private final ClassLoader delegate = ClassLoader.getSystemClassLoader();

    public DelegatingLocalClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return delegate.loadClass(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return delegate.getResources(name);
    }

    @Nullable
    @Override
    public URL getResource(String name) {
        return delegate.getResource(name);
    }

    @Override
    public Stream<URL> resources(String name) {
        return delegate.resources(name);
    }

    /**
     * This is here to hint Classgraph of the URLs for all classes. This is called, reflectively, by the "Fallback"
     * classloader handler within Classgraph.
     *
     * @return an array of {@link URL}s making up the system classpath.
     */
    public URL[] getURLs() {
        return getSystemClasspath();
    }

}
