package com.namazustudios.socialengine;

@FunctionalInterface
public interface MessageReader {

    <T> T read(final byte[] message, Class<?> type);
}

