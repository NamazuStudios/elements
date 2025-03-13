package dev.getelements.elements.sdk.local;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.local.SystemClasspathUtils.getSystemClasspath;

/**
 * Delegates to the {@link #getSystemClassLoader()} using it for all loading.
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

    public URL[] getURLs() {
        return getSystemClasspath();
    }

}
