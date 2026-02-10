package dev.getelements.elements.sdk.model;

import dev.getelements.elements.sdk.PermittedTypes;
import dev.getelements.elements.sdk.annotation.ElementPrivate;

import java.util.Set;

@ElementPrivate
public class ModelPermittedTypes implements PermittedTypes {

    private static final Set<String> PERMITTED_TYPES = Set.of(
            // More information as to why this is here:
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=398723
            "org.eclipse.persistence.internal.jaxb.WrappedValue"
    );

    @Override
    public String getDescription() {
        return "Permits org.eclipse.persistence.internal.jaxb.WrappedValue due to known issue with Eclipse's " +
               "implementation of JAXb / MOXy - https://bugs.eclipse.org/bugs/show_bug.cgi?id=398723";
    }

    @Override
    public boolean test(final Class<?> aClass) {
        return PERMITTED_TYPES.contains(aClass.getName());
    }

}
