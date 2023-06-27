package dev.getelements.elements.rt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by patricktwohig on 7/24/15.
 */
public class SimpleResponseHeader implements ResponseHeader, Serializable {

    private int code = UNKNOWN_SEQUENCE;

    private int sequence;

    private Map<String, List<Object>> headers;

    @Override
    public List<String> getHeaderNames() {
        return new ArrayList<>(headers.keySet());
    }

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
    public Optional<List<Object>> getHeaders(final String name) {
        final List<Object> value = headers.get(name);
        return value == null || value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    public void setHeaders(Map<String, List<Object>> headers) {
        this.headers = headers;
    }

    @Override
    public void copyToMap(final Map<String, List<Object>> requestHeaderMap) {
        for (final Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            requestHeaderMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
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
        return headers != null ? headers.equals(that.headers) : that.headers == null;
    }

    @Override
    public int hashCode() {
        int result = getCode();
        result = 31 * result + getSequence();
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }
}
