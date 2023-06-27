package dev.getelements.elements.exception;

/**
 * An enumeration of error codes which may provide greater detail as to why
 * a problem occurred.
 *
 * Created by patricktwohig on 4/10/15.
 */
public enum  ErrorCode {
    /**
     * An object or resource was not found.
     */
    NOT_FOUND,

    /**
     * Access to a given resource is forbidden.
     */
    FORBIDDEN,

    /**
     * Access to a given resource is unauthorized.
     */
    UNAUTHORIZED,

    /**
     * Indicates an invalid parameter was passed to the API
     */
    INVALID_PARAMETER,

    /**
     * There already exists a resource of this type.
     */
    DUPLICATE,

    /**
     * The request conflicts with existing data for some reason.
     */
    CONFLICT,

    /**
     * Invalid data or input was provided.
     */
    INVALID_DATA,

    /**
     * A particular resource is overloaded at the moment.
     */
    OVERLOAD,

    /**
     * The particular feature is not implemented or is currently unavailable.
     */
    NOT_IMPLEMENTED,

    /**
     * An external resource failed to behave ideally.
     */
    EXTERNAL_RESOURCE_FAILED,

    /**
     * Healths checks failed.
     */
    UNHEALTHY,

    /**
     * Some other exception.
     */
    UNKNOWN

}
