package com.namazustudios.socialengine.rt.remote;

@FunctionalInterface
public interface MessageWriter {

    byte[] write(final Object object);

}
