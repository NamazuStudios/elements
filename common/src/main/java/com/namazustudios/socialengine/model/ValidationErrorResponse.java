package com.namazustudios.socialengine.model;

import java.util.List;

/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidationErrorResponse extends ErrorResponse {

    private List<String> validationFailureMessages;

    public List<String> getValidationFailureMessages() {
        return validationFailureMessages;
    }

    public void setValidationFailureMessages(List<String> validationFailureMessages) {
        this.validationFailureMessages = validationFailureMessages;
    }

}
