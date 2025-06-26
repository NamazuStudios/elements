package dev.getelements.elements.sdk.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import static dev.getelements.elements.sdk.local.SystemClasspathUtils.getSystemClasspath;

public class DelegatingPreloadClassLoader extends ClassLoader {

    private final ClassLoader delegate = ClassLoader.getSystemClassLoader();

    @Override
    protected Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException {

        final var loaded = doLoadClass(name);

        if (resolve)
            resolveClass(loaded);

        return loaded;

    }

    private Class<?> doLoadClass(final String name) throws ClassNotFoundException {

        final var filen = name.replace('.', '/') + ".class";

        try (final var is = delegate.getResourceAsStream(filen);
             final var bos = new ByteArrayOutputStream()) {

            if (is == null) {
                throw new ClassNotFoundException(name);
            }

            is.transferTo(bos);

            final var array = bos.toByteArray();
            return defineClass(name, array, 0, array.length);

        } catch (IOException ex) {
            throw new ClassNotFoundException(name);
        }

    }

    /// This is here to hint Classgraph of the URLs for all classes. This is called, reflectively, by the "Fallback"
    /// classloader handler within Classgraph.
    ///
    /// @return an array of [URL]s making up the system classpath.
    public URL[] getURLs() {
        return getSystemClasspath();
    }

}
