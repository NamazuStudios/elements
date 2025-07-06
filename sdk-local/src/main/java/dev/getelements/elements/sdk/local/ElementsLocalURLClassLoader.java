package dev.getelements.elements.sdk.local;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This has to share some of the interfaces with the SDK, this includes the bare minimum types necessary to start
 * the services. This only includes the types that are defined in the sdk and sdk-local libraries as that is in
 * common with the bootstrapping code. Everything else must be loaded via the SDK.
 */
public class ElementsLocalURLClassLoader extends URLClassLoader {

    private static final ClassLoader SYSTEM = ClassLoader.getSystemClassLoader();

    private static final Set<String> CORE_SDK_PACKAGES = Set.of(
            "org.sfl4j",
            "org.sfl4j.spi",
            "org.sfl4j.event",
            "org.sfl4j.helpers",
            "dev.getelements.elements.sdk",
            "dev.getelements.elements.sdk.local",
            "dev.getelements.elements.sdk.annotation",
            "dev.getelements.elements.sdk.exception",
            "dev.getelements.elements.sdk.query",
            "dev.getelements.elements.sdk.record"
    );

    private final Set<String> types;

    private final Set<String> packages;

    private ElementsLocalURLClassLoader(final ClassLoader parent,
                                        final List<URL> classpath,
                                        final Set<String> types,
                                        final Set<String> packages) {
        super("Local Elements SDK", classpath.toArray(URL[]::new), parent);
        this.types = types;
        this.packages = packages;
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

        final var aClass = types.contains(name) || isSharedPackage(name)
                ? SYSTEM.loadClass(name)
                : super.loadClass(name, resolve);

        if (resolve)
            resolveClass(aClass);

        return aClass;

    }

    private boolean isSharedPackage(final String name) {
        final var lastDot = name.lastIndexOf('.');
        final var packageName = (lastDot >= 0) ? name.substring(0, lastDot) : "";
        return packages.contains(packageName);
    }

    public static class Builder {

        private ClassLoader parent;

        private final Set<String> types = new HashSet<>();

        private final Set<String> packages = new HashSet<>();

        public Builder withType(final String type) {
            types.add(type);
            return this;
        }

        public Builder withPackage(final String packageName) {
            packages.add(packageName);
            return this;
        }

        public Builder withPackages(final Collection<String> packagesToAdd) {
            packages.addAll(packagesToAdd);
            return this;
        }

        public Builder withCoreSdkPackages() {
            packages.addAll(CORE_SDK_PACKAGES);
            return this;
        }

        public Builder inheritedFrom(final ElementsLocalURLClassLoader localSdkURLClassLoader) {
            types.addAll(localSdkURLClassLoader.types);
            packages.addAll(localSdkURLClassLoader.packages);
            return this;
        }

        public Builder withParent(final ClassLoader parent) {
            this.parent = parent;
            return this;
        }

        public ElementsLocalURLClassLoader build(final List<URL> classpath) {
            return new ElementsLocalURLClassLoader(parent, classpath, types, packages);
        }

    }


}
