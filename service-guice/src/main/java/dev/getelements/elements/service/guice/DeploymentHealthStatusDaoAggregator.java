package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.sdk.dao.DeploymentHealthStatusDao;

import java.util.Set;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * Aggregates the DAOs which report on Element deployment health. Modules which host an {@link
 * dev.getelements.elements.sdk.deployment.ElementRuntimeService} contribute an implementation. Unlike {@link
 * DatabaseHealthStatusDaoAggregator} this does not require an implementation to be bound, because deployments are only
 * observable where a runtime service exists. When the set is empty the deployment health check is a no-op.
 */
public class DeploymentHealthStatusDaoAggregator extends PrivateModule {

    @Override
    protected void configure() {
        newSetBinder(binder(), DeploymentHealthStatusDao.class);
        expose(new TypeLiteral<Set<DeploymentHealthStatusDao>>(){});
    }

}
