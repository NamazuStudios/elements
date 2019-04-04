package com.namazustudios.socialengine.appserve.srv;

import java.util.Objects;

public class SrvUniqueIdentifier {

    final private String host;
    final private int port;

    public SrvUniqueIdentifier(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }


    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SrvUniqueIdentifier that = (SrvUniqueIdentifier) o;
        return getPort() == that.getPort() &&
                Objects.equals(getHost(), that.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getPort());
    }

    @Override
    public String toString() {
        return "SrvUniqueIdentifier{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
