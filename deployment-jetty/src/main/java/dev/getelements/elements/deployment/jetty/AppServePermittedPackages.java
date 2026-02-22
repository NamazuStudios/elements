package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.PermittedPackages;

import java.util.List;

public class AppServePermittedPackages implements PermittedPackages {

    private static final List<String> PERMITTED_PACKAGES = List.of(
            "jakarta.ws.rs",
            "jakarta.servlet",
            "jakarta.websocket",
            "jakarta.validation",
            "io.swagger.v3.oas.annotations"
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
        return "Permits RESTFul Web Services (jakarta rs) and WebSockets (jakarta.websocket) as well as validation " +
               "(jakarta.validation). Additionally permits OAS types to be used within an Element. This includes " +
               "io.swagger.v3.oas annotations";
    }

}
