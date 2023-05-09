package dev.getelements.elements.model;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by patricktwohig on 5/7/15.
 */
@ApiModel
public class ValidationErrorResponse extends ErrorResponse implements Serializable {

    private List<String> validationFailureMessages;

    public List<String> getValidationFailureMessages() {
        return validationFailureMessages;
    }

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
