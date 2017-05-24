package com.namazustudios.socialengine.model;

import io.swagger.annotations.ApiModel;

/**
 * Created by patricktwohig on 4/10/15.
 */
@ApiModel
public class ErrorResponse {

    private String code;

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

}
