package dev.getelements.elements.app.serve;

import dev.getelements.elements.app.serve.loader.Loader;
import dev.getelements.elements.common.app.AbstractApplicationDeploymentService;
import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static dev.getelements.elements.common.app.ApplicationDeploymentService.DeploymentStatus.*;

public class JettyApplicationDeploymentService extends AbstractApplicationDeploymentService {

    private Set<Loader> loaders;

    private ApplicationDao applicationDao;

    @Override
    public List<DeploymentRecord> deployAvailableApplications() {
            return getApplicationDao()
                .getActiveApplications()
                .stream()
                .map(this::deployApplication)
                .toList();
    }

    @Override
    protected DeploymentRecord doDeployment(final Application application,
                                            final ApplicationElementRecord record) {

        final var uris = new TreeSet<URI>();
        final var logs = new ArrayList<String>();
        final var warnings = new ArrayList<String>();
        final var errors = new ArrayList<Throwable>();
        final var pending = new Loader.PendingDeployment(uris::add, logs::add, warnings::add, errors::add);

        getLoaders().forEach(loader -> {
            try {
                loader.load(pending, record);
            } catch (Throwable th) {
                pending.error(th);
            }
        });

        DeploymentStatus status = CLEAN;

        if (!warnings.isEmpty()) {
            status = WARNINGS;
        }

        if (!errors.isEmpty()) {
            status = UNSTABLE;
        }

        return new DeploymentRecord(
                application,
                status,
                record,
                Set.copyOf(uris),
                List.copyOf(logs),
                List.copyOf(errors)
        );

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
