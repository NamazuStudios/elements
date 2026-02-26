package dev.getelements.elements.sdk;

public class JakartaInjectPermittedPackages implements PermittedPackages {

    @Override
    public boolean test(final Package aPackage) {
        return aPackage.getName().startsWith("jakarta.inject");
    }

    @Override
    public String getDescription() {
        return "Permits the usage of jakarta.inject types.";
    }

}
