package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementLocal;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<ElementPackageRecord> additionalPackages,
        Class<? extends ElementLoader> loader) {

    /**
     * A shortcut to get the {@link Package} name.
     * @return the {@link Package} name.
     */
    public String pkgName() {
        return pkg.getName();
    }

    /**
     * Iterates over all defined packages in this {@link ElementDefinition}, calling the {@link Consumer} for each
     * defined (including the defining package).
     *
     * @param recursive called for package defined as recursive
     * @param nonRecursive called for a pakage defined as non-recursive
     */
    public void acceptPackages(
            final Consumer<String> recursive,
            final Consumer<String> nonRecursive) {

        if (recursive())
            recursive.accept(pkgName());
        else
            nonRecursive.accept(pkgName());

        additionalPackages.forEach(p -> {
            if (p.recursive())
                recursive.accept(p.name());
            else
                nonRecursive.accept(p.name());
        });

    }

    /**
     * Checks if the supplied {@link Class} is part of the {@link Element} attached to this record.
     *
     * @param aClass a {@link Class}
     * @return true if part of this {@link Element}, false otherwise
     */
    public boolean isPartOfElement(final Class<?> aClass) {
        final var aPackage = aClass.getPackage();
        return isPartOfElement(aPackage);
    }

    /**
     * Checks if the supplied {@link Package} is part of the {@link Element} attached to this record.
     *
     * @param aPackage a {@link Package}
     * @return true if part of this {@link Element}, false otherwise
     */
    public boolean isPartOfElement(final Package aPackage) {
        if (aPackage == null) {
            return false;
        } else if (recursive()) {
            return aPackage.getName().startsWith(pkgName());
        } else if (aPackage.getName().equals(pkgName())) {
            return true;
        } else {
            return additionalPackages()
                    .stream()
                    .anyMatch(epr -> epr.isPartOfElement(aPackage));
        }
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

        final var additionalPackages = Stream.of(anElementDefinition.additionalPackages())
                .map(ElementPackageRecord::from)
                .toList();

        return new ElementDefinitionRecord(
                aPackage,
                name,
                anElementDefinition.recursive(),
                additionalPackages,
                anElementDefinition.loader()
        );

    }

}
