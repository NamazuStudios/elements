package dev.getelements.elements.service;

import dev.getelements.elements.sdk.PermittedPackages;

public class JakartaMailPermittedPackages implements PermittedPackages {

    @Override
    public boolean test(final Package aPackage) {
        return aPackage.getName().startsWith("jakarta.mail");
    }

    @Override
    public String getDescription() {
        return "Permits the usage of jakarta.mail types.";
    }

}
