package dev.getelements.elements.sdk.model.annotation;

import dev.getelements.elements.sdk.model.application.Application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aggregates several {@link FacebookPermission} instances which will be used to
 * build the list of default permissions used by an {@link Application}.  Various
 * services may require specific permissions that are not otherwise configurable
 * by the user for the application access.
 *
 * Created by patricktwohig on 6/14/17.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FacebookPermissions {

    /**
     * The list of permissions.
     *
     * @return the list of permissions
     */
    FacebookPermission[] value();

}
