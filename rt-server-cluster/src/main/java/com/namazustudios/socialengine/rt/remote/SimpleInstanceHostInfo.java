package com.namazustudios.socialengine.rt.remote;

public class SimpleInstanceHostInfo implements InstanceHostInfo {

    private final String connectAddress;

    public SimpleInstanceHostInfo(final String connectAddress) {
        this.connectAddress = connectAddress;
    }

    @Override
    public String getConnectAddress() {
        return connectAddress;
    }

    @Override
    public int hashCode() {
        return InstanceHostInfo.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return InstanceHostInfo.equals(this, obj);
    }

    @Override
    public String toString() {
        return "SimpleInstanceHostInfo{" +
            "connectAddress='" + connectAddress + '\'' +
            '}';
    }

}
