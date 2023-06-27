package dev.getelements.elements.rt;

import java.util.UUID;

/**
 * A simple error model to indicate that an exception was caught and is being returned to the client.
 *
 * Created by patricktwohig on 7/31/15.
 */
public class SimpleExceptionResponsePayload implements ExceptionResponsePayload {

    public String message;

    private String uuid = UUID.randomUUID().toString();

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "SimpleExceptionResponsePayload{" +
                "message='" + message + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final SimpleExceptionResponsePayload simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();

        public Builder uuid(final String uuid) {
            simpleExceptionResponsePayload.setUUID(uuid);
            return this;
        }

        public Builder message(final String message) {
            simpleExceptionResponsePayload.setMessage(message);
            return this;
        }

        public Builder from(final Exception exception) {
            return message(exception.getMessage());
        }

        public SimpleExceptionResponsePayload build() {
            return simpleExceptionResponsePayload;
        }

    }

}
