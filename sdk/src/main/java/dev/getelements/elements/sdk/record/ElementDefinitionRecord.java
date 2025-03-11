package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.annotation.ElementDefinition;

/**
 * Represents the record pertaining to the {@link ElementDefinition} annotation.
 *
 * @param name the name of the element
 * @param recursive true, if recursive
 * @param loader the loader typ
 */
public record ElementDefinitionRecord(
        Package pkg,
        String name,
        boolean recursive,
        Class<? extends ElementLoader> loader) {

    /**
     * A shortcut to get the {@link Package} name.
     * @return the {@link Package} name.
     */
    public String pkgName() {
        return pkg.getName();
    }

    /**
     * Gets a {@link ElementDefinitionRecord} from a {@link Package}
     * @param aPackage a package
     * @return a {@link ElementDefinitionRecord}
     */
    public static ElementDefinitionRecord fromPackage(final Package aPackage) {

        final ElementDefinition anElementDefinition = aPackage.getAnnotation(ElementDefinition.class);

        if (anElementDefinition == null) {
            throw new IllegalArgumentException(aPackage.getName() + " does not have an element definition.");
        }

        final var name = anElementDefinition.value().isBlank()
                ? aPackage.getName()
                : anElementDefinition.value();

        return new ElementDefinitionRecord(
                aPackage,
                name,
                anElementDefinition.recursive(),
                anElementDefinition.loader()
        );

    }

}
