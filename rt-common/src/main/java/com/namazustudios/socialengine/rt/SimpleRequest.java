package com.namazustudios.socialengine.rt;

import java.util.*;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class SimpleRequest implements Request {

    private SimpleRequestHeader header;

    private Object payload;

    @Override
    public SimpleRequestHeader getHeader() {
        return header;
    }

    public void setHeader(final SimpleRequestHeader header) {
        this.header = header;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    public void setPayload(final Object payload) {
        this.payload = payload;
    }

    @Override
    public <T> T getPayload(Class<T> cls) {
        return cls.cast(getPayload());
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

        final Map<String, List<Object> > simpleRequestHeaderMap = new LinkedHashMap<>();

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

                if (request.getHeader().getHeaderNames() != null) {

                    final LinkedHashMap<String, List<Object>> headers = new LinkedHashMap<>();

                    for (final String header : request.getHeader().getHeaderNames()) {
                        final List<Object> value = request.getHeader().getHeaders(header);
                        headers.put(header, new ArrayList<>(value));
                    }

                    simpleRequestHeader.setHeaders(headers);

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
         * Sets the path of the request.
         *
         * @param path
         * @return
         */
        public Builder path(final String path) {
            simpleRequestHeader.setPath(path);
            return this;
        }

        /**
         * Sets the method of this request.
         *
         * @param method the method
         * @return this object
         */
        public Builder method(final String method) {
            simpleRequestHeader.setMethod(method);
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

        /**
         * Sets a header for the request
         *
         * @param key the key
         * @param value the value
         * @return this object
         */
        public Builder header(final String key, final String value) {
            simpleRequestHeaderMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            return this;
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
