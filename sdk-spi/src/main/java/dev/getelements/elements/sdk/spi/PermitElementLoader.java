package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.ElementLoaderFactory;
import dev.getelements.elements.sdk.PermittedTypes;

public class PermitElementLoader implements PermittedTypes {

    @Override
    public boolean test(final Class<?> aClass) {
        return ElementLoader.class.isAssignableFrom(aClass) ||
               ElementLoaderFactory.class.isAssignableFrom(aClass);
    }

}
