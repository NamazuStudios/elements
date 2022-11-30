package com.namazustudios.socialengine.service.formidium.api;

import java.io.Serializable;
import java.util.Objects;

public class FormidiumApiResponse implements Serializable {

    private String status;

    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        if (o == null || getClass() != o.getClass()) return false;
        FormidiumApiResponse that = (FormidiumApiResponse) o;
        return Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getMessage());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FormidiumApiResponse{");
        sb.append("status='").append(status).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
