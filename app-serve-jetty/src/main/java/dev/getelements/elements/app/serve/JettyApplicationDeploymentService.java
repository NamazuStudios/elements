package dev.getelements.elements.app.serve;

import dev.getelements.elements.app.serve.loader.Loader;
import dev.getelements.elements.common.app.AbstractApplicationDeploymentService;
import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.rt.exception.ApplicationCodeNotFoundException;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class JettyApplicationDeploymentService extends AbstractApplicationDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(JettyApplicationDeploymentService.class);

    private Set<Loader> loaders;

    private ApplicationDao applicationDao;

    @Override
    public void deployAvailableApplications() {
            getApplicationDao()
                .getActiveApplications()
                .stream()
                .forEach(this::tryDeployApplication);
    }

    private void tryDeployApplication(final Application application) {
        try {
            deployApplication(application);
        } catch (ApplicationCodeNotFoundException ex) {
            logger.info("No code for application {} ({}).", application.getName(), application.getId());
        } catch (Exception | LinkageError ex) {
            logger.error("Unable to deploy application {} ({}).", application.getName(), application.getId(), ex);
        }
    }

    @Override
    protected void doDeployment(final ApplicationElementRecord record) {
        getLoaders().forEach(loader -> loader.load(record));
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
