package dev.getelements.elements.common.app;

import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.exception.ApplicationCodeNotFoundException;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.common.app.ApplicationDeploymentService.DeploymentRecord.fail;
import static java.lang.String.format;

public abstract class AbstractApplicationDeploymentService implements ApplicationDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractApplicationDeploymentService.class);

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

        final ApplicationElementRecord applicationElementRecord;

        try {
            applicationElementRecord = getApplicationElementService().getOrLoadApplication(application);
        } catch (ApplicationCodeNotFoundException ex) {

            final var logs = List.of(format("No application code found: %s - %s",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            ));

            try (final var mon = Monitor.enter(lock)) {
                return deployments.computeIfAbsent(application.getId(), aid -> fail(application, logs, ex));
            }

        } catch (Exception ex) {

            logger.error("Unable to deploy application {} ({}).", application.getName(), application.getId(), ex);

            final var logs = List.of(format("Unable to deploy: %s - %s",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            ));

            try (final var mon = Monitor.enter(lock)) {
                return deployments.computeIfAbsent(application.getId(), aid -> fail(application, logs, ex));
            }

        } catch (LinkageError ex) {

            logger.error("Unable to deploy application {} ({}).", application.getName(), application.getId(), ex);

            final var logs = List.of(
                    format("LinkageError during application deployment: %s - %s",
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                    ),
                    "Check that @ElementPublic was added to all public facing interfaces and types."
            );

            try (final var mon = Monitor.enter(lock)) {
                return deployments.computeIfAbsent(application.getId(), aid -> fail(application, logs, ex));
            }

        }

        try (final var mon = Monitor.enter(lock)) {
            return deployments.computeIfAbsent(application.getId(), applicationId -> {

                final var deploymentRecord =  doDeployment(application, applicationElementRecord);

                deploymentRecord.logs().forEach(log -> logger.info(
                        "Log during deployment of application {} ({}): {}",
                        application.getName(),
                        application.getId(),
                        log
                ));

                deploymentRecord.errors().forEach(error -> logger.error(
                        "Error during deployment of application {} ({}): {}",
                        application.getName(),
                        application.getId(),
                        error.getMessage(),
                        error
                ));

                return deploymentRecord;

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
    protected abstract DeploymentRecord doDeployment(final Application application,
                                                     final ApplicationElementRecord record);

    public ApplicationElementService getApplicationElementService() {
        return applicationElementService;
    }

    @Inject
    public void setApplicationElementService(ApplicationElementService applicationElementService) {
        this.applicationElementService = applicationElementService;
    }

}
