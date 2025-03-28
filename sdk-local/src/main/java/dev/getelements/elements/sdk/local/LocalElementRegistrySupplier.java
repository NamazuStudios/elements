package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ElementRegistrySupplier;

public class LocalElementRegistrySupplier implements ElementRegistrySupplier {

    private static ElementRegistry instance;

    public static ElementRegistry getInstance() {
        return instance;
    }

    public static void setInstance(ElementRegistry instance) {
        LocalElementRegistrySupplier.instance = instance;
    }

    @Override
    public ElementRegistry get() {
        return instance;
    }

}
