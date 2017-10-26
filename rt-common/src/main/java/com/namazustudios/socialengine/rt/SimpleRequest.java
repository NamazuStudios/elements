package com.namazustudios.socialengine.rt;

import com.google.common.collect.ListMultimap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class SimpleRequest implements Request {

    private SimpleRequestHeader header;

    private Object payload;

    private Map<String, List<Object>> parameterMap;

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
    public List<String> getParameterNames() {
        return parameterMap.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public List<Object> getParameters(String parameterName) {
        return parameterMap.get(parameterName);
    }

    public void setParameterMap(Map<String, List<Object>> parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleRequest)) return false;

        SimpleRequest that = (SimpleRequest) o;

        if (getHeader() != null ? !getHeader().equals(that.getHeader()) : that.getHeader() != null) return false;
        if (getPayload() != null ? !getPayload().equals(that.getPayload()) : that.getPayload() != null) return false;
        return getParameterMap() != null ? getParameterMap().equals(that.getParameterMap()) : that.getParameterMap() == null;
    }

    @Override
    public int hashCode() {
        int result = getHeader() != null ? getHeader().hashCode() : 0;
        result = 31 * result + (getPayload() != null ? getPayload().hashCode() : 0);
        result = 31 * result + (getParameterMap() != null ? getParameterMap().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleRequest{" +
                "header=" + header +
                ", payload=" + payload +
                '}';
    }

    /**
     * Shorthand for using new Builder().
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

        private String path;

        private String method;

        private int sequence = RequestHeader.UNKNOWN_SEQUENCE;

        private Object payload;

        private ParameterizedPath parameterizedPath;

        final Map<String, List<Object> > simpleRequestHeaderMap = new LinkedHashMap<>();

        final Map<String, List<Object> > simpleRequestParameterMap = new LinkedHashMap<>();

        /**
         * Builds an instance of {@link SimpleRequest} copying all vauues fromt he given {@link Request}.
         *
         * @param request the request
         *
         * @return this object
         */
        public Builder from(final Request request) {

            if (request.getHeader() != null) {

                payload = request.getPayload();

                simpleRequestParameterMap.clear();

                for (final String parameterName : request.getParameterNames()) {
                    final List<Object> value = request.getParameters(parameterName);
                    simpleRequestParameterMap.put(parameterName, new ArrayList<>(value));
                }

                final RequestHeader header = request.getHeader();

                path = header.getPath();
                method = header.getMethod();
                sequence = header.getSequence();

                simpleRequestHeaderMap.clear();

                for (final String headerName : header.getHeaderNames()) {
                    final List<Object> value = request.getHeader().getHeaders(headerName);
                    simpleRequestHeaderMap.put(headerName, new ArrayList<>(value));
                }

            }

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
         * Sets the path of the request.
         *
         * @param path
         * @return
         */
        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the method of this request.
         *
         * @param method the method
         * @return this object
         */
        public Builder method(final String method) {
            this.method = method;
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
         * Sets a header for the request
         *
         * @param key the key
         * @param value the value
         * @return this object
         */
        public Builder header(final String key, final Object value) {
            simpleRequestHeaderMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            return this;
        }

        /**
         * Sets a parameter for the request.
         *
         * @param key the key
         * @param value the value
         * @return this object
         */
        public Builder parameter(final String key, final Object value) {
            simpleRequestParameterMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            return this;
        }

        /**
         * Calls {@link #parameterizedPath(ParameterizedPath)} using the {@link String} representation of the
         * {@link ParameterizedPath}.
         *
         * @param parameterizedPathString the {@link String} representing the {@link ParameterizedPath}
         * @return this instance
         */
        public Builder parameterizedPath(final String parameterizedPathString) {
            return parameterizedPath(new ParameterizedPath(parameterizedPathString));
        }

        /**
         * Sets a {@link ParameterizedPath} which will be used by the {@link SimpleRequestHeader} to extract
         * {@link Path} parameters using {@link ParameterizedPath#extract(Path)}.  If left unspecified, then the
         * {@link SimpleRequestHeader} will simply return an empty map.
         *
         * @param parameterizedPath the {@link ParameterizedPath} to use, or null
         * @return this object
         */
        public Builder parameterizedPath(final ParameterizedPath parameterizedPath) {
            this.parameterizedPath = parameterizedPath;
            return this;
        }

        /**
         * Builds the {@link SimpleResponse} object.
         *
         * @return the {@link SimpleResponse}
         */
        public SimpleRequest build() {

            final SimpleRequest simpleRequest = new SimpleRequest();
            final SimpleRequestHeader simpleRequestHeader = new SimpleRequestHeader();

            simpleRequest.setPayload(payload);
            simpleRequest.setParameterMap(new LinkedHashMap<>(simpleRequestParameterMap));

            simpleRequestHeader.setPath(path);
            simpleRequestHeader.setMethod(method);
            simpleRequestHeader.setSequence(sequence);
            simpleRequestHeader.setParameterizedPath(parameterizedPath);
            simpleRequestHeader.setHeaders(new LinkedHashMap<>(simpleRequestHeaderMap));

            simpleRequest.setHeader(simpleRequestHeader);

            return simpleRequest;

        }

    }

}
