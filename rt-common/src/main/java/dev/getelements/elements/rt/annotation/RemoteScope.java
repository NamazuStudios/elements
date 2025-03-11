package dev.getelements.elements.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.getelements.elements.rt.annotation.CaseFormat.NATURAL;

/**
 * Provides scoping metadata for an exposed service. For an object to be remotely exposed, it must bear one of these
 * annotations or it will be forbidden based on the container configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteScope {

    /**
     * The core Elements scope.
     */
    String API_SCOPE = "eci:api";

    /**
     * The master node scope.
     */
    String MASTER_SCOPE = "eci:master";

    /**
     * The worker node scope.
     */
    String WORKER_SCOPE = "eci:worker";

    /**
     * The Elements Native RT Protocol
     */
    String ELEMENTS_RT_PROTOCOL = "eci:rt";

    /**
     * The JSON-RPC Protocol tunneled via HTTP.
     */
    String ELEMENTS_JSON_RPC_PROTOCOL = "eci:json-rpc";

    /**
     * Indicates the scope for remote dispatch
     */
    String REMOTE_SCOPE = "dev.getelements.elements.rt.scope";

    /**
     * Indicates the protocol for remote dispatch
     */
    String REMOTE_PROTOCOL = "dev.getelements.elements.rt.protocol";

    /**
     * Indicates the scope of the service. Note scopes prefixed with "eci" are reserved for system use.
     *
     * @return the scope.
     */
    String scope();

    /**
     * Indicates the protocol of the service. Note protocols prefixed with "eci" are reserved for system use.
     *
     * @return the scope.
     */
    String protocol();

    /**
     * Indicates the module's code style. By default this enforces natural code style.
     *
     * @return the {@link CodeStyle}
     */
    CodeStyle style() default @CodeStyle(
        methodCaseFormat = NATURAL,
        constantCaseFormat = NATURAL,
        parameterCaseFormat = NATURAL
    );

    /**
     * Indicates if this service is deprecated.
     *
     * @return the {@link DeprecationDefinition}
     */
    DeprecationDefinition deprecated() default @DeprecationDefinition(deprecated = false);

}
