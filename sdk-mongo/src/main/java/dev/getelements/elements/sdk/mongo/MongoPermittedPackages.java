package dev.getelements.elements.sdk.mongo;

import dev.getelements.elements.sdk.PermittedPackages;

import java.util.List;

public class MongoPermittedPackages implements PermittedPackages {

    private static final List<String> PERMITTED_PACKAGES = List.of(
            "dev.morphia",
            "com.mongodb",
            "org.bson"
    );

    @Override
    public boolean test(final Package aPackage) {
        final var aPackageName = aPackage.getName();
        return PERMITTED_PACKAGES
                .stream()
                .anyMatch(aPackageName::startsWith);
    }

    @Override
    public String getDescription() {
        return "Permits Morphia (dev.morphia) and the MongoDB driver (com.mongodb, org.bson) types so that "
                + "Elements can consume the Datastore, MongoClient, and MongoDatabase services exported by this "
                + "package with matching class identity.";
    }

}
