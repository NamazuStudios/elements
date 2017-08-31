package com.namazustudios.socialengine.rt;

import java.util.List;
import java.util.Map;

/**
 * Created by patricktwohig on 7/24/15.
 */
public class SimpleResponseHeader implements ResponseHeader {

    private int code = UNKNOWN_SEQUENCE;

    private int sequence;

    private Map<String, List<Object>> headers;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public Map<String, List<Object>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<Object>> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "SimpleResponseHeader{" +
                "code=" + code + "(" + ResponseCode.getDescriptionFromCode(code) +")" +
                ", sequence=" + sequence +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleResponseHeader)) return false;

        SimpleResponseHeader that = (SimpleResponseHeader) o;

        if (getCode() != that.getCode()) return false;
        if (getSequence() != that.getSequence()) return false;
        return getHeaders() != null ? getHeaders().equals(that.getHeaders()) : that.getHeaders() == null;
    }

    @Override
    public int hashCode() {
        int result = getCode();
        result = 31 * result + getSequence();
        result = 31 * result + (getHeaders() != null ? getHeaders().hashCode() : 0);
        return result;
    }

}
