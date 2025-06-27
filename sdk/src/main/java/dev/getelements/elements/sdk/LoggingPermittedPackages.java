package dev.getelements.elements.sdk;

import java.util.List;

public class LoggingPermittedPackages implements PermittedPackages {

    private static final List<String> PERMITTED_PACKAGES = List.of(
            "org.slf4j",
            "org.slf4j.spi",
            "org.slf4j.event",
            "org.slf4j.helpers"
    );

    @Override
    public boolean test(final Package aPackage) {
        return PERMITTED_PACKAGES.contains(aPackage.getName());
    }

}
