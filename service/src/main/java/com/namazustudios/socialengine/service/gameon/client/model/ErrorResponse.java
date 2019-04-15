package com.namazustudios.socialengine.service.gameon.client.model;

public class ErrorResponse {

    int errorCode;
    String message;
    String request_id;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int code) {
        this.errorCode = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }
}
