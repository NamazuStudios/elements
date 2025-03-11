package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.Context;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.util.ShutdownHooks;

import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachingContextFactory implements Context.Factory {

    private final ShutdownHooks shutdownHooks = new ShutdownHooks(CachingContextFactory.class);

    private final Map<ApplicationId, Context> cache = new ConcurrentHashMap<>();

    private Function<ApplicationId, Context> applicationContextSupplier;

    @Override
    public Context getContextForApplication(final ApplicationId applicationId) {
        return cache.computeIfAbsent(applicationId, k -> {
            final Context context = getApplicationContextSupplier().apply(k);
            shutdownHooks.add(context, context::shutdown);
            return context;
        });
    }

    public Function<ApplicationId, Context> getApplicationContextSupplier() {
        return applicationContextSupplier;
    }

    @Inject
    public void setApplicationContextSupplier(final Function<ApplicationId, Context> applicationContextSupplier) {
        this.applicationContextSupplier = applicationContextSupplier;
    }

}
