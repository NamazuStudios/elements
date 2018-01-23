package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by patricktwohig on 7/31/15.
 */
public class SimpleResponse implements Response, Serializable {

    private SimpleResponseHeader responseHeader;

    private Object payload;

    @Override
    public SimpleResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(SimpleResponseHeader responseHeader) {
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

        private Object payload;

        private int code;

        private int sequence = ResponseHeader.UNKNOWN_SEQUENCE;

        final Map<String, List<Object>> simpleResponseHeaderMap = new LinkedHashMap<>();

        /**
         * Creates a new builder for {@link SimpleResponse}
         */
        public Builder() {}

        /**
         * Builds an instance of {@link SimpleResponse} in response to the given {@link Request}.
         *
         * @param request the request
         *
         * @return this object
         */
        public Builder from(final Request request) {
            sequence = request.getHeader().getSequence();
            return this;
        }

        /**
         * Copies all values from the given {@link Response}.
         *
         * @param response the response object
         * @return this object
         */
        public Builder from(final Response response) {

            payload = response.getPayload();

            final ResponseHeader header = response.getResponseHeader();

            code = header.getCode();
            sequence = header.getSequence();

            simpleResponseHeaderMap.clear();

            for (final String headerName : header.getHeaderNames()) {
                final List<Object> value = header.getHeaders(headerName);
                simpleResponseHeaderMap.put(headerName, new ArrayList<>(value));
            }

            return this;

        }

        /**
         * Sets the response {@link ResponseHeader#getCode()} value.
         *
         * @return this object
         */
        public Builder ok() {
            code = ResponseCode.OK.getCode();
            return this;
        }

        /**
         * Sets the response {@link ResponseHeader#getCode()} value to one of {@link ResponseCode}.
         *
         * @return this object
         */
        public Builder code(final ResponseCode code) {
            this.code = code.getCode();
            return this;
        }

        /**
         * Sets the response {@link ResponseHeader#getCode()} value to a custom response code.
         *
         * @return this object
         */
        public Builder code(final int code) {
            this.code = code;
            return this;
        }

        /**
         * Sets the sequence of the response.
         *
         * @param sequence the sequence.
         * @return this object.
         */
        public Builder sequence(final int sequence) {
            this.sequence = sequence;
            return this;
        }

        /**
         * Sets the value of {@link Response#getPayload()}.
         *
         * @param payload the payload instance
         * @return this object
         */
        public Builder payload(final Object payload) {
            this.payload = payload;
            return this;
        }

        /**
         * Appends the header with name and value.
         *
         * @param header the header name
         * @param value the header value
         */
        public Builder header(final String header, final Object value) {
            simpleResponseHeaderMap.computeIfAbsent(header, k -> new ArrayList<>()).add(value);
            return this;
        }

        /**
         * Builds the {@link SimpleResponse} object.
         *
         * @return the {@link SimpleResponse}
         */
        public SimpleResponse build() {

            final SimpleResponse simpleResponse = new SimpleResponse();
            final SimpleResponseHeader simpleResponseHeader = new SimpleResponseHeader();

            simpleResponse.setResponseHeader(simpleResponseHeader);
            simpleResponse.setPayload(payload);

            simpleResponseHeader.setHeaders(new LinkedHashMap<>(simpleResponseHeaderMap));

            return simpleResponse;
        }

    }

}
