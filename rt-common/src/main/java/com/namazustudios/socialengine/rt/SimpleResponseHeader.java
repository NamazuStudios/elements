package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 7/24/15.
 */
public class SimpleResponseHeader implements ResponseHeader {

    int code;

    int sequence;

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
        return getSequence() == that.getSequence();

    }

    @Override
    public int hashCode() {
        int result = getCode();
        result = 31 * result + getSequence();
        return result;
    }

}
