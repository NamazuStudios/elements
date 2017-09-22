package com.namazustudios.socialengine.appserve;

import com.google.inject.Injector;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DispatcherAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherAppProvider.class);

    private final ConcurrentMap<String, Injector> applicationInjectorMap = new ConcurrentHashMap<>();

    private DeploymentManager deploymentManager;

    private Injector injector;

    private ApplicationService applicationService;

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        return null;
    }

    @Override
    protected void doStart() throws Exception {
        getApplicationService().getApplications().getObjects().forEach(this::start);
    }

    private void start(final Application application) {

    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

}
