package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * Simple implementation of the response.
 *
 * Created by patricktwohig on 7/24/15.
 */
public class SimpleRequestHeader implements RequestHeader, Serializable {

    private int sequence;

    private String method;

    private String path;

    private Map<String, List<Object> > headers;

    private ParameterizedPath parameterizedPath;

    @Override
    public List<String> getHeaderNames() {
        return new ArrayList<>(headers.keySet());
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public Optional<List<Object>> getHeaders(final String name) {
        final List<Object> objects = headers.get(name);
        return objects == null || objects.isEmpty() ? Optional.empty() : Optional.of(headers.get(name));
    }

    public void setHeaders(Map<String, List<Object> > headers) {
        this.headers = headers;
    }

    @Override
    public Map<String, String> getPathParameters() {
        if (parameterizedPath == null) {
            return emptyMap();
        } else {
            return parameterizedPath.extract(getParsedPath());
        }
    }

    public ParameterizedPath getParameterizedPath() {
        return parameterizedPath;
    }

    public void setParameterizedPath(ParameterizedPath parameterizedPath) {
        this.parameterizedPath = parameterizedPath;
    }

    @Override
    public void copyToMap(final Map<String, List<Object>> requestHeaderMap) {
        for (final Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            requestHeaderMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    @Override
    public String toString() {
        return "SimpleRequestHeader{" +
                "sequence=" + sequence +
                ", method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", simpleResponseHeaderMap=" + headers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleRequestHeader)) return false;

        SimpleRequestHeader that = (SimpleRequestHeader) o;

        if (getSequence() != that.getSequence()) return false;
        if (getMethod() != null ? !getMethod().equals(that.getMethod()) : that.getMethod() != null) return false;
        if (getPath() != null ? !getPath().equals(that.getPath()) : that.getPath() != null) return false;
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
        return getParameterizedPath() != null ? getParameterizedPath().equals(that.getParameterizedPath()) : that.getParameterizedPath() == null;
    }

    @Override
    public int hashCode() {
        int result = getSequence();
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (getParameterizedPath() != null ? getParameterizedPath().hashCode() : 0);
        return result;
    }
}
