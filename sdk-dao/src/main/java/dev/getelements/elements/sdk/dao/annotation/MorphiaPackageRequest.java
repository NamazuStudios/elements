package dev.getelements.elements.sdk.dao.annotation;

import dev.getelements.elements.sdk.PackageRequest;
import dev.getelements.elements.sdk.annotation.ElementPackageRequest;

/**
 * A {@link PackageRequest} implementation that grants an Element access to the Morphia and MongoDB
 * package namespaces required to use Morphia {@code @Entity} classes.
 *
 * <p>Declare on a {@code package-info.java} package declaration:
 *
 * <pre>{@code
 * @ElementPackageRequest(request = MorphiaPackageRequest.class)
 * package com.example.myelement;
 *
 * import dev.getelements.elements.sdk.annotation.ElementPackageRequest;
 * import dev.getelements.elements.sdk.dao.annotation.MorphiaPackageRequest;
 * }</pre>
 *
 * <p>This grants wildcard access to:
 * <ul>
 *   <li>{@code dev.morphia.*} — Morphia annotations and APIs</li>
 *   <li>{@code com.mongodb.*} — MongoDB Java driver</li>
 *   <li>{@code org.bson.*} — BSON library</li>
 * </ul>
 *
 * <p>Elements that use the MongoDB driver directly without Morphia should use
 * {@link PackageRequest.Wildcard} with an explicit {@link ElementPackageRequest#value()} listing
 * only the packages they actually need.
 */
public class MorphiaPackageRequest implements PackageRequest {

    private static final String[] PREFIXES = {
        "dev.morphia",
        "com.mongodb",
        "org.bson",
    };

    @Override
    public boolean test(final ElementPackageRequest annotation, final String packageName) {
        for (final var prefix : PREFIXES) {
            if (packageName.equals(prefix) || packageName.startsWith(prefix + ".")) {
                return true;
            }
        }
        return false;
    }

}
