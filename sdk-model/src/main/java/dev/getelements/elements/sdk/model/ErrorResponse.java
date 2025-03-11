package dev.getelements.elements.sdk.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Created by patricktwohig on 4/10/15.
 */
@Schema
public class ErrorResponse implements Serializable {

    @Schema(description = "A machine readable code of the error.")
    private String code;

    @Schema(description =
            "A description of the error. This error is not intended to be displayed to the end-user, " +
            "rather it is it designed to relay information to the application developer."
    )
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

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
