package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.annotation.ElementLocal;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import static dev.getelements.elements.sdk.local.SystemClasspathUtils.getSystemClasspath;

/// Delegates to the [ClassLoader] returned by [#getSystemClassLoader()] using it for all loading and reads
/// the system classpath for locating all resources. To properly simulate the levels of isolation provided by the
/// deployment, this will copy the classes from the System Class Path, and define them independently of what is loaded
/// by the main classpath. Therefore, when using this classloader, there could be some incompatibility from classes
/// loaded by the system and within the individual Elements. However, this level of isolation should be equivalent to
/// what is found in the main run time.
///
/// Prior to loading, this type must be injected with the [ElementDefinitionRecord] in order to properly filter out
/// types which do not belong to the Element. This may actually enforce stricter than usual rules than when running in
/// the server environment. However, should result in the same behavior in a properly configured Element.
public class DelegatingLocalClassLoader extends ClassLoader {

    private ElementDefinitionRecord elementDefinitionRecord;

    private final ClassLoader delegate = ClassLoader.getSystemClassLoader();

    public DelegatingLocalClassLoader(final ClassLoader parent) {
        super(parent);
    }

    public ElementDefinitionRecord getElementDefinitionRecord() {
        return elementDefinitionRecord;
    }

    public void setElementDefinitionRecord(ElementDefinitionRecord elementDefinitionRecord) {
        this.elementDefinitionRecord = elementDefinitionRecord;
    }

    @Override
    protected Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException {

        if (getElementDefinitionRecord() == null) {
            // Without an ElementRecord, we can't properly filter out system classes from the classes
            // specific to the Element. Therefore, we throw this exception. The class loading process
            // will later replace the value set to this classloader as not to avoid class
            throw new IllegalStateException("No element record found.");
        }

        Class<?> aClass = delegate.loadClass(name);

        if (aClass.isAnnotationPresent(ElementLocal.class)) {
            return getParent().loadClass(name);
        } else if (getElementDefinitionRecord().isPartOfElement(aClass)) {

            aClass = doLoadClass(name);
            if (resolve)
                resolveClass(aClass);

        // TODO Replace this with a proper SPI Check instead of the hacks below
        } else if (aClass.getPackageName().startsWith("dev.getelements.elements.sdk.spi")) {
            aClass = doLoadClass(name);
            if (resolve)
                resolveClass(aClass);
        } else if (aClass.getPackageName().startsWith("com.google.inject")) {
            aClass = doLoadClass(name);
            if (resolve)
                resolveClass(aClass);
        }

        return aClass;

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
