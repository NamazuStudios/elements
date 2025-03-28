package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.annotation.ElementLocal;
import dev.getelements.elements.sdk.record.ElementRecord;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
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

    private ElementRecord elementRecord;

    private final ClassLoader delegate = ClassLoader.getSystemClassLoader();

    public DelegatingLocalClassLoader(final ClassLoader parent) {
        super(parent);
    }

    public ElementRecord getElementRecord() {
        return elementRecord;
    }

    public void setElementRecord(ElementRecord elementRecord) {
        this.elementRecord = elementRecord;
    }

    @Override
    protected Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException {

        Class<?> aClass = delegate.loadClass(name);

        if (aClass.getAnnotation(ElementLocal.class) != null) {
            return getParent().loadClass(name);
        }

        if (getElementRecord() != null && getElementRecord().definition().isPartOfElement(aClass)) {

            try {
                aClass = getPlatformClassLoader().loadClass(name);
            } catch (ClassNotFoundException ex) {
                aClass = doLoadClass(name, resolve);
            }

            if (resolve) {
                resolveClass(aClass);
            }

        }

        return aClass;

    }

    private Class<?> doLoadClass(final String name, boolean resolve) throws ClassNotFoundException {

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
