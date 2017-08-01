package com.namazustudios.socialengine.rt;

import java.util.List;
import java.util.Map;

/**
 * Simple implementation of the response.
 *
 * Created by patricktwohig on 7/24/15.
 */
public class SimpleRequestHeader implements RequestHeader {

    private int sequence;

    private String method;

    private String path;

    private Map<String, List<String> > headers;

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
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String> > headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "SimpleRequestHeader{" +
                "sequence=" + sequence +
                ", method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers=" + headers +
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
        return !(getHeaders() != null ? !getHeaders().equals(that.getHeaders()) : that.getHeaders() != null);

    }

    @Override
    public int hashCode() {
        int result = getSequence();
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
        result = 31 * result + (getHeaders() != null ? getHeaders().hashCode() : 0);
        return result;
    }

}
