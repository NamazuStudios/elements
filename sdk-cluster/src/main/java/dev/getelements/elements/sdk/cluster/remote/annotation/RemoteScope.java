package dev.getelements.elements.sdk.cluster.remote.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* Provides scoping metadata for an exposed service. This allows for a service to declare scopes for a service. This is
* for notational and selecting purposes. For example, one may wish to limit dispatch to a particular scope when
* remotely issuing invocations.
*/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteScope {

    /**
     * The wildcard scope. When used in the annotation, this indicates that the scope is global and therefore exists
     * in all other scopes. When used in matching, it will match all scopes including itself. It can be used in
     * conjunction with a prefix. For example "dev.getelements:*" would match all scopes which start with
     * "dev.getelements:" which woudl comprise the system scopes only.
     */
    String WILDCARD_SCOPE = "*";

    /**
     * The system scope prefix. Only core system scopes may use this prefix. Using this prefix outside of the SDK
     * internals could cause undefined behavior in the selection process.
     */
    String SYSTEM_SCOPE_PREFIX = "dev.getelements:";

    /**
     * Indicates the scope of the service. Note, scopes prefixed with @{{@link RemoteScope#SYSTEM_SCOPE_PREFIX}} are
     * reserved for system use. We recommend using RDNS notation as to avoid collissions as much as possible across
     * multiple vendors.
     *
     * @return the scope.
     */
    String value();

    /**
     * Indicates if this service is deprecated.
     *
     * @return the {@link DeprecationDefinition}
     */
    DeprecationDefinition deprecated() default @DeprecationDefinition(deprecated = false);

}
