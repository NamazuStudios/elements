package dev.getelements.elements.rt;

import com.google.common.collect.ListMultimap;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class SimpleRequest implements Request, Serializable {

    private String uuid;

    private SimpleRequestHeader header;

    private SimpleAttributes attributes;

    private Object payload;

    private Map<String, List<Object>> parameterMap;

    private String toString;

    @Override
    public String getId() {
        return uuid == null ? (uuid = randomUUID().toString()) : uuid;
    }

    public void setId(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public SimpleRequestHeader getHeader() {
        return header;
    }

    public void setHeader(final SimpleRequestHeader header) {
        this.header = header;
    }

    @Override
    public SimpleAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(SimpleAttributes attributes) {
        this.attributes = attributes;
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
        return toString == null ? "SimpleRequest{" +
                "header=" + header +
                ", payload=" + payload +
                '}' : toString;
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

        private String id;

        private String path;

        private String method;

        private int sequence = RequestHeader.UNKNOWN_SEQUENCE;

        private Object payload;

        private ParameterizedPath parameterizedPath;

        private String toString;

        private final Map<String, Object> simpleAttributesMap = new HashMap<>();

        private final Map<String, List<Object> > simpleRequestHeaderMap = new LinkedHashMap<>();

        private final Map<String, List<Object> > simpleRequestParameterMap = new LinkedHashMap<>();

        /**
         * Builds an instance of {@link SimpleRequest} copying all vauues fromt he given {@link Request}.
         *
         * @param request the request
         *
         * @return this object
         */
        public Builder from(final Request request) {

            id = request.getId();

            payload = request.getPayload();

            simpleRequestParameterMap.clear();

            for (final String parameterName : request.getParameterNames()) {
                final List<Object> value = request.getParameters(parameterName);
                simpleRequestParameterMap.put(parameterName, value == null ? Collections.emptyList() : new ArrayList<>(value));
            }

            final RequestHeader header = request.getHeader();

            if (header != null) {


                path = header.getPath();
                method = header.getMethod();
                sequence = header.getSequence();
                toString = request.toString();

                simpleRequestHeaderMap.clear();
                request.getHeader().copyToMap(simpleRequestHeaderMap);

                simpleAttributesMap.clear();
                request.getAttributes().copyToMap(simpleAttributesMap);

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
         * Sets the attribute with the supplied name and value.
         *
         * @param attribute the attribute name
         * @param value the attribute value
         * @return this object
         */
        public Builder attribute(final String attribute, final Object value) {
            simpleAttributesMap.put(attribute, value);
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
            final SimpleAttributes simpleAttributes = new SimpleAttributes();

            simpleRequest.setId(id);
            simpleRequest.setPayload(payload);
            simpleRequest.setParameterMap(new LinkedHashMap<>(simpleRequestParameterMap));


            simpleAttributes.setAttributes(new HashMap<>(simpleAttributesMap));
            simpleAttributes.setAttribute(REQUEST_ID_ATTRIBUTE, id);

            simpleRequest.setAttributes(simpleAttributes);

            simpleRequestHeader.setPath(path);
            simpleRequestHeader.setMethod(method);
            simpleRequestHeader.setSequence(sequence);
            simpleRequestHeader.setParameterizedPath(parameterizedPath);
            simpleRequestHeader.setHeaders(new LinkedHashMap<>(simpleRequestHeaderMap));

            simpleRequest.setHeader(simpleRequestHeader);
            simpleRequest.toString = format("Simple Request For (%s)", toString);

            return simpleRequest;

        }

    }

}
