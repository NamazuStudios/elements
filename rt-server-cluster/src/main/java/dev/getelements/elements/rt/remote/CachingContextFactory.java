package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.Context;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.sdk.util.ShutdownHooks;

import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachingContextFactory implements Context.Factory {

    private final ShutdownHooks shutdownHooks = new ShutdownHooks(CachingContextFactory.class);

    private final Map<DeploymentId, Context> cache = new ConcurrentHashMap<>();

    private Function<DeploymentId, Context> deploymentContextSupplier;

    @Override
    public Context getContextForApplication(final DeploymentId deploymentId) {
        return cache.computeIfAbsent(deploymentId, k -> {
            final Context context = getDeploymentContextSupplier().apply(k);
            shutdownHooks.add(context, context::shutdown);
            return context;
        });
    }

    public Function<DeploymentId, Context> getDeploymentContextSupplier() {
        return deploymentContextSupplier;
    }

    @Inject
    public void setDeploymentContextSupplier(final Function<DeploymentId, Context> deploymentContextSupplier) {
        this.deploymentContextSupplier = deploymentContextSupplier;
    }

}
