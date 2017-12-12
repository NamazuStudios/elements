package com.namazustudios.socialengine;

@FunctionalInterface
public interface MessageWriter {

    byte[] write(final Object object);

}
