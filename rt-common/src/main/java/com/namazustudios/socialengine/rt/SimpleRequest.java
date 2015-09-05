package com.namazustudios.socialengine.rt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class SimpleRequest<PayloadT> implements Request {

    private RequestHeader header;

    private PayloadT payload;

    @Override
    public RequestHeader getHeader() {
        return header;
    }

    public void setHeader(RequestHeader header) {
        this.header = header;
    }

    @Override
    public PayloadT getPayload() {
        return payload;
    }

    public void setPayload(PayloadT payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "SimpleRequest{" +
                "header=" + header +
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
     * Used to build instances of {@link SimpleRequest} easily.
     */
    public static class Builder {

        final SimpleRequest simpleRequest = new SimpleRequest();

        final SimpleRequestHeader simpleRequestHeader = new SimpleRequestHeader();

        final Map<String, String> simpleRequestHeaderMap = new HashMap<String, String >();

        /**
         * Creates a new builder for {@link SimpleResponse}
         */
        public Builder() {
            simpleRequest.setHeader(simpleRequestHeader);
            simpleRequestHeader.setHeaders(simpleRequestHeaderMap);
        }

        /**
         * Builds an instance of {@link SimpleRequest} copying all vauues fromt he given {@link Request}.
         *
         * @param request the request
         *
         * @return this object
         */
        public Builder from(final Request request) {

            if (request.getHeader() != null) {

                simpleRequestHeader.setPath(request.getHeader().getPath());
                simpleRequestHeader.setMethod(request.getHeader().getMethod());
                simpleRequestHeader.setSequence(request.getHeader().getSequence());

                if (request.getHeader().getHeaders() != null) {
                    simpleRequestHeader.setHeaders(new LinkedHashMap<>(request.getHeader().getHeaders()));
                }

            }

            simpleRequest.setPayload(request.getPayload());

            return this;

        }

        /**
         * Sets the sequence of the response.
         *
         * @param sequence the sequence.
         * @return this object.
         */
        public Builder sequence(final int sequence) {
            simpleRequestHeader.setSequence(sequence);
            return this;
        }

        /**
         * Sets the value of {@link Response#getPayload()}.
         *
         * @param payload the payload instance
         * @return this object
         */
        public Builder payload(final Object payload) {
            simpleRequest.setPayload(payload);
            return this;
        }

        public String addHeader(final String key, final String value) {
            return simpleRequestHeaderMap.put(key, value);
        }

        /**
         * Builds the {@link SimpleResponse} object.
         *
         * @return the {@link SimpleResponse}
         */
        public SimpleRequest build() {
            return simpleRequest;
        }

    }

}
