package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.rt.Context;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DefaultContextFactory implements ContextFactory {

    private final Map<String, Context> cache = new ConcurrentHashMap<>();

    private Function<String, Context> applicationContextSupplier;

    @Override
    public Context getContextForApplication(final String applicationId) {
        return cache.computeIfAbsent(applicationId, getApplicationContextSupplier());
    }

    public Function<String, Context> getApplicationContextSupplier() {
        return applicationContextSupplier;
    }

    @Inject
    public void setApplicationContextSupplier(Function<String, Context> applicationContextSupplier) {
        this.applicationContextSupplier = applicationContextSupplier;
    }

}
