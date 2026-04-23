package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.PermittedPackages;

/**
 * Permits {@code dev.morphia.*} packages through the {@link dev.getelements.elements.sdk.PermittedTypesClassLoader},
 * making Morphia types (e.g. {@code Datastore}, {@code MorphiaSession}) accessible to custom Elements.
 *
 * <p>This allows Elements that declare {@code @ElementDependency("dev.getelements.elements.sdk.dao")} to inject
 * {@code Datastore} without any additional classloader configuration.
 */
public class MorphiaPermittedPackages implements PermittedPackages {

    @Override
    public boolean test(final Package aPackage) {
        return aPackage.getName().startsWith("dev.morphia");
    }

    @Override
    public String getDescription() {
        return "Permits Morphia (dev.morphia.*) types so that custom Elements can inject Datastore " +
               "and other Morphia types when depending on the DAO element.";
    }

}
