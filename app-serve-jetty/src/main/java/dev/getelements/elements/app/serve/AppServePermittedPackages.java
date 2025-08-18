package dev.getelements.elements.app.serve;

import dev.getelements.elements.sdk.PermittedPackages;

import java.util.List;

public class AppServePermittedPackages implements PermittedPackages {

    private static final List<String> PERMITTED_PACKAGES = List.of(
            "jakarta.ws.rs",
            "jakarta.servlet",
            "jakarta.websocket",
            "io.swagger.v3.oas.annotations"
    );

    @Override
    public boolean test(final Package aPackage) {
        final var aPackageName = aPackage.getName();
        return PERMITTED_PACKAGES
                .stream()
                .anyMatch(aPackageName::startsWith);
    }

}
