package com.namazustudios.socialengine.model;

import io.swagger.annotations.ApiModel;

import java.util.List;

/**
 * Created by patricktwohig on 5/7/15.
 */
@ApiModel
public class ValidationErrorResponse extends ErrorResponse {

    private List<String> validationFailureMessages;

    public List<String> getValidationFailureMessages() {
        return validationFailureMessages;
    }

    public void setValidationFailureMessages(List<String> validationFailureMessages) {
        this.validationFailureMessages = validationFailureMessages;
    }

}
