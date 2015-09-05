package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 7/31/15.
 */
public class SimpleResponse implements Response {

    private ResponseHeader responseHeader;

    private Object payload;

    @Override
    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "SimpleResponse{" +
                "responseHeader=" + responseHeader +
                ", payload=" + payload +
                '}';
    }

    /**
     * Shorthand to ussing {@link Builder#Builder()}.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Used to build instances of {@link SimpleResponse} easily.
     */
    public static class Builder {

        final SimpleResponse simpleResponse = new SimpleResponse();

        final SimpleResponseHeader simpleResponseHeader = new SimpleResponseHeader();

        /**
         * Creates a new builder for {@link SimpleResponse}
         */
        public Builder() {
            simpleResponse.setResponseHeader(simpleResponseHeader);
        }

        /**
         * Builds an instance of {@link SimpleResponse} in response to the given {@link Request}.
         *
         * @param request the request
         *
         * @return this object
         */
        public Builder from(final Request request) {

            if (request.getHeader() != null) {
                simpleResponseHeader.setSequence(request.getHeader().getSequence());
            }

            return this;

        }

        /**
         * Copies all values from the given {@link Response}.
         *
         * @param response the response object
         * @return this object
         */
        public Builder from(final Response response) {
            simpleResponse.setPayload(response.getPayload());

            if (response.getResponseHeader() != null) {
                simpleResponseHeader.setCode(response.getResponseHeader().getCode());
                simpleResponseHeader.setSequence(response.getResponseHeader().getSequence());
            }

            return this;
        }

        /**
         * Sets the response {@link ResponseHeader#getCode()} value.
         *
         * @return this object
         */
        public Builder ok() {
            simpleResponseHeader.setCode(ResponseCode.OK.getCode());
            return this;
        }

        /**
         * Sets the response {@link ResponseHeader#getCode()} value to one of {@link ResponseCode}.
         *
         * @return this object
         */
        public Builder code(final ResponseCode code) {
            simpleResponseHeader.setCode(code.getCode());
            return this;
        }

        /**
         * Sets the response {@link ResponseHeader#getCode()} value to a custom response code.
         *
         * @return this object
         */
        public Builder code(final int code) {
            simpleResponseHeader.setCode(code);
            return this;
        }

        /**
         * Sets the sequence of the response.
         *
         * @param sequence the sequence.
         * @return this object.
         */
        public Builder sequence(final int sequence) {
            simpleResponseHeader.setSequence(sequence);
            return this;
        }

        /**
         * Sets the value of {@link Response#getPayload()}.
         *
         * @param payload the payload instance
         * @return this object
         */
        public Builder payload(final Object payload) {
            simpleResponse.setPayload(payload);
            return this;
        }

        /**
         * Builds the {@link SimpleResponse} object.
         *
         * @return the {@link SimpleResponse}
         */
        public SimpleResponse build() {
            return simpleResponse;
        }

    }

}
