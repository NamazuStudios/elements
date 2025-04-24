package dev.getelements.elements.sdk.model;

import dev.getelements.elements.sdk.PermittedPackages;
import dev.getelements.elements.sdk.annotation.ElementPrivate;

import java.util.List;

@ElementPrivate
public class ModelPermittedPackages implements PermittedPackages {

    private static final List<String> PERMITTED_PACKAGES = List.of(
            "jakarta.inject",
            "jakarta.validation"
    );

    @Override
    public boolean test(final Package aPackage) {
        return PERMITTED_PACKAGES.contains(aPackage.getName());
    }

}
