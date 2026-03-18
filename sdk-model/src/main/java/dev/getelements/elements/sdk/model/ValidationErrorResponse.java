package dev.getelements.elements.sdk.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an error response that includes validation failure details.
 */
@Schema
public class ValidationErrorResponse extends ErrorResponse implements Serializable {

    /** Creates a new instance. */
    public ValidationErrorResponse() {}

    private List<String> validationFailureMessages;

    /**
     * Returns the list of validation failure messages.
     * @return the validation failure messages
     */
    public List<String> getValidationFailureMessages() {
        return validationFailureMessages;
    }

    /**
     * Sets the list of validation failure messages.
     * @param validationFailureMessages the validation failure messages
     */
    public void setValidationFailureMessages(List<String> validationFailureMessages) {
        this.validationFailureMessages = validationFailureMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationErrorResponse)) return false;
        if (!super.equals(o)) return false;

        ValidationErrorResponse that = (ValidationErrorResponse) o;

        return getValidationFailureMessages() != null ? getValidationFailureMessages().equals(that.getValidationFailureMessages()) : that.getValidationFailureMessages() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getValidationFailureMessages() != null ? getValidationFailureMessages().hashCode() : 0);
        return result;
    }

}
