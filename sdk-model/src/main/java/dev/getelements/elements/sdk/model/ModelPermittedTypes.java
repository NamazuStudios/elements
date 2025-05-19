package dev.getelements.elements.sdk.model;

import dev.getelements.elements.sdk.PermittedTypes;
import dev.getelements.elements.sdk.annotation.ElementPrivate;

import java.util.Set;

@ElementPrivate
public class ModelPermittedTypes implements PermittedTypes {

    private static final Set<String> PERMITTED_TYPES = Set.of(
            // More information as to why this is here:
            // https://www.eclipse.org/forums/index.php/t/594323/
            "org.eclipse.persistence.internal.jaxb.WrappedValue"
    );

    @Override
    public boolean test(final Class<?> aClass) {
        return PERMITTED_TYPES.contains(aClass.getName());
    }

}
