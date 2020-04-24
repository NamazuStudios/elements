package com.namazustudios.socialengine.rt.transact;

import java.nio.file.Path;

public interface RevisionFactory {

    <T> Revision<T> create(String at, T value);

    <T> Revision<T> create(Revision<?> at, T value);

}
