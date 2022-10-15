package com.namazustudios.socialengine.jrpc.provider;

import javax.inject.Provider;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.emptyList;

public abstract class AbstractRandomUrlProvider implements Provider<String> {

    protected List<String> urls = emptyList();

    @Override
    public String get() {

        final var tlr = ThreadLocalRandom.current();

        if (urls.isEmpty()) {
            throw new IllegalStateException("No urls configured.");
        }

        final var index = tlr.nextInt(urls.size());
        return urls.get(index);

    }

}
