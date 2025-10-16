package dev.getelements.elements.common.app;

import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class AbstractApplicationDeploymentService implements ApplicationDeploymentService {

    private ApplicationElementService applicationElementService;

    /**
     * The lock to use for synchronizing deployments. This is a service-wide lock, meaning that only one deployment can
     * happen at a time.
     */
    protected final Lock lock = new ReentrantLock();

    private final Map<String, DeploymentRecord> deployments = new LinkedHashMap<>();

    @Override
    public List<DeploymentRecord> listAllDeployments() {
        try (final var mon = Monitor.enter(lock)) {
            return List.copyOf(deployments.values());
        }
    }

    @Override
    public DeploymentRecord deployApplication(final Application application) {
        try (final var mon = Monitor.enter(lock)) {
            return deployments.computeIfAbsent(application.getId(), applicationId -> {
                final var record = getApplicationElementService().getOrLoadApplication(application);
                return doDeployment(record);
            });
        }
    }

    /**
     * Performs the actual deployment of the {@link ApplicationElementRecord}. This is called within the context of a
     * lock on this service, so implementations do not need to provide their own locking.
     *
     * @param record the result of loading the {@link Application}
     * @return the result of the deployment
     */
    protected abstract DeploymentRecord doDeployment(final ApplicationElementRecord record);

    public ApplicationElementService getApplicationElementService() {
        return applicationElementService;
    }

    @Inject
    public void setApplicationElementService(ApplicationElementService applicationElementService) {
        this.applicationElementService = applicationElementService;
    }

}
