package dev.getelements.elements.sdk.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Represents an error response returned by the API.
 */
@Schema
public class ErrorResponse implements Serializable {

    /** Creates a new instance. */
    public ErrorResponse() {}

    @Schema(description = "A machine readable code of the error.")
    private String code;

    @Schema(description =
            "A description of the error. This error is not intended to be displayed to the end-user, " +
            "rather it is it designed to relay information to the application developer."
    )
    private String message;

    /**
     * Returns the machine-readable error code.
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the machine-readable error code.
     * @param code the error code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Returns the error message.
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorResponse)) return false;

        ErrorResponse that = (ErrorResponse) o;

        if (getCode() != null ? !getCode().equals(that.getCode()) : that.getCode() != null) return false;
        return getMessage() != null ? getMessage().equals(that.getMessage()) : that.getMessage() == null;
    }

    @Override
    public int hashCode() {
        int result = getCode() != null ? getCode().hashCode() : 0;
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        return result;
    }

}
