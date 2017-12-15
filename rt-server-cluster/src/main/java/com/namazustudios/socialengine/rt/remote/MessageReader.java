package com.namazustudios.socialengine.rt.remote;

@FunctionalInterface
public interface MessageReader {

    <T> T read(final byte[] message, Class<?> type);
}

