package dev.getelements.elements.sdk.local;

import java.util.List;

public class FilteringBaseClassLoader extends ClassLoader {

    private final ClassLoader base;

    public FilteringBaseClassLoader() {
        this(getSystemClassLoader());
    }

    public FilteringBaseClassLoader(final ClassLoader base) {
        super(null);
        this.base = base;
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ex) {
            // Ignore and continue to load from the base class loader
        }

        final var aClass = base.loadClass(name);

        if (aClass.getPackage().getName().startsWith("dev.getelements.elements.crossfire")) {
            throw new ClassNotFoundException(name);
        }

        final var matching = List.of(
                "jakarta.inject",
                "org.aopalliance",
                "com.google.common.util.concurrent",
                "dev.getelements.elements.sdk.spi.guice"
        );

        if (aClass.getName().equals("dev.getelements.elements.sdk.spi.guice.GuiceElementLoader")) {
            throw new ClassNotFoundException(name);
        } else if (matching.contains(aClass.getPackage().getName())) {
            throw new ClassNotFoundException(name);
        } else if (aClass.getPackage().getName().startsWith("com.google.common")) {
            throw new ClassNotFoundException(name);
        } else if (aClass.getPackage().getName().startsWith("com.google.inject")) {
            throw new ClassNotFoundException(name);
        } else if (aClass.getPackage().getName().startsWith("dev.getelements.elements.crossfire")) {
            throw new ClassNotFoundException(name);
        }

        return aClass;

    }

}
