package dev.getelements.elements.sdk.spi.guice;

import dev.getelements.elements.sdk.PermittedPackages;

public class PermittedGuicePackages implements PermittedPackages {

    @Override
    public boolean test(final Package aPackage) {
        return aPackage.getName().equals("jakarta.inject") ||
               aPackage.getName().startsWith("com.google.inject");
    }

}
