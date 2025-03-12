package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.model.application.Application;

/**
 * Represents a local application element record.
 * @param applicationNameOrId the application name or ID
 * @param packageName the package name
 * @param attributes the attributes to use when loading the package
 */
public record LocalApplicationElementRecord(
        String applicationNameOrId,
        String packageName,
        Attributes attributes) {

    public boolean matches(final Application application) {
        return applicationNameOrId().equals(application.getId()) ||
               applicationNameOrId().equals(application.getName());
    }

}
