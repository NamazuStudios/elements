package dev.getelements.elements.app.serve;

import dev.getelements.elements.app.serve.loader.Loader;
import dev.getelements.elements.common.app.AbstractApplicationDeploymentService;
import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.rt.exception.ApplicationCodeNotFoundException;
import dev.getelements.elements.rt.exception.ApplicationDeploymentException;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static dev.getelements.elements.common.app.ApplicationDeploymentService.DeploymentStatus.DEPLOYED;

public class JettyApplicationDeploymentService extends AbstractApplicationDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(JettyApplicationDeploymentService.class);

    private Set<Loader> loaders;

    private ApplicationDao applicationDao;

    @Override
    public List<DeploymentRecord> deployAvailableApplications() {
            return getApplicationDao()
                .getActiveApplications()
                .stream()
                .map(this::tryDeployApplication)
                .toList();
    }

    private DeploymentRecord tryDeployApplication(final Application application) {
        try {
            return deployApplication(application);
        } catch (ApplicationCodeNotFoundException ex) {
            logger.info("No code for application {} ({}).", application.getName(), application.getId());
            final var logs = List.of("No application code found.");
            return DeploymentRecord.fail(logs, ex);
        } catch (ApplicationDeploymentException ex) {
            return DeploymentRecord.fail(ex.getLogs(), ex.getCauses());
        } catch (Exception ex) {
            logger.error("Unable to deploy application {} ({}).", application.getName(), application.getId(), ex);
            final var logs = List.of("No application code found.");
            return DeploymentRecord.fail(logs, ex);
        } catch (LinkageError ex) {

            logger.error("Unable to deploy application {} ({}).", application.getName(), application.getId(), ex);

            final var logs = List.of(
                    "Caught LinkageError Deploying application: " + application.getId(),
                    "Check that @ElementPublic was added to all public facing interfaces and types."
            );

            return DeploymentRecord.fail(logs, ex);

        }
    }

    @Override
    protected DeploymentRecord doDeployment(final ApplicationElementRecord record) {

        final var uris = new TreeSet<URI>();
        final var logs = new ArrayList<String>();
        final var errors = new ArrayList<Throwable>();
        final var pending = new Loader.PendingDeployment(uris::add, logs::add, errors::add);

        getLoaders().forEach(loader -> {
            try {
                loader.load(pending, record);
            } catch (Throwable th) {
                errors.add(th);
            }
        });

        return new DeploymentRecord(DEPLOYED, record, Set.copyOf(uris), List.copyOf(logs), List.copyOf(errors));

    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public Set<Loader> getLoaders() {
        return loaders;
    }

    @Inject
    public void setLoaders(final Set<Loader> loaders) {
        this.loaders = loaders;
    }

}
