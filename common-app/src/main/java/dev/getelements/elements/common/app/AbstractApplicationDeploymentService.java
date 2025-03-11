package dev.getelements.elements.common.app;

import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.sdk.model.application.Application;
import jakarta.inject.Inject;

public abstract class AbstractApplicationDeploymentService implements ApplicationDeploymentService {

    private ApplicationElementService applicationElementService;

    @Override
    public void deployApplication(final Application application) {
        final var record = getApplicationElementService().getOrLoadApplication(application);
        doDeployment(record);
    }

    protected abstract void doDeployment(final ApplicationElementRecord record);

    public ApplicationElementService getApplicationElementService() {
        return applicationElementService;
    }

    @Inject
    public void setApplicationElementService(ApplicationElementService applicationElementService) {
        this.applicationElementService = applicationElementService;
    }

}
