package dev.getelements.elements.sdk.local;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This has to share some of the interfaces with the SDK, this includes the bare minimum types necessary to start
 * the services. This only includes the types that are defined in the sdk and sdk-local libraries as that is in
 * common with the bootstrapping code. Everything else must be loaded via the SDK.
 */
public class LocalSdkURLClassLoader extends URLClassLoader {

    private static final ClassLoader SYSTEM = ClassLoader.getSystemClassLoader();

    private static final Set<String> CORE_SDK_PACKAGES = Set.of(
            "dev.getelements.elements.sdk",
            "dev.getelements.elements.sdk.local",
            "dev.getelements.elements.sdk.annotation",
            "dev.getelements.elements.sdk.exception",
            "dev.getelements.elements.sdk.query",
            "dev.getelements.elements.sdk.record"
    );

    private final Set<String> packages;

    private LocalSdkURLClassLoader(final List<URL> classpath, final Set<String> packages) {
        super("Local Elements SDK", classpath.toArray(URL[]::new), null);
        this.packages = packages;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ex) {
            return doLoadClass(name, resolve);
        }
    }

    private Class<?> doLoadClass(final String name, final boolean resolve) throws ClassNotFoundException {

        final var aClass = SYSTEM.loadClass(name);

        if (!packages.contains(aClass.getPackage().getName()))
            throw new ClassNotFoundException(name);

        if (resolve)
            resolveClass(aClass);

        return aClass;

    }

    public static class Builder {

        private final Set<String> packages = new HashSet<>();

        public Builder withPackage(final String packageName) {
            packages.add(packageName);
            return this;
        }

        public Builder withCoreSdkPackages() {
            packages.addAll(CORE_SDK_PACKAGES);
            return this;
        }

        public LocalSdkURLClassLoader build(final List<URL> classpath) {
            return new LocalSdkURLClassLoader(classpath, packages);
        }

    }

}
