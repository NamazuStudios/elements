package com.namazustudios.socialengine.formidium;

import com.google.inject.Injector;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import javax.inject.Inject;
import javax.inject.Named;

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.formidium.FormidiumConstants.FORMIDIUM_API_URL;
import static com.namazustudios.socialengine.formidium.FormidiumConstants.FORMIDIUM_CONTEXT_ROOT;
import static com.namazustudios.socialengine.servlet.security.HttpPathUtils.normalize;
import static java.lang.String.format;

public class FormidiumAppProvider extends AbstractLifeCycle implements AppProvider {

    private Injector injector;

    private String rootContext;

    private String formidiumApiUrl;

    private String formidiumContext;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() throws Exception {

        final var formidiumContextRoot = normalize(format("%s/%s", getRootContext(), getFormidiumContext()));
        deploymentManager.addApp(new App(deploymentManager, this, formidiumContextRoot, buildFormidiumApiContext()));

    }

    private ContextHandler buildFormidiumApiContext() {
        return null;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        return null;
    }

    public String getRootContext() {
        return rootContext;
    }

    @Inject
    public void setRootContext(@Named(HTTP_PATH_PREFIX) String rootContext) {
        this.rootContext = rootContext;
    }

    public String getFormidiumApiUrl() {
        return formidiumApiUrl;
    }

    @Inject
    public void setFormidiumApiUrl(@Named(FORMIDIUM_API_URL) String formidiumApiUrl) {
        this.formidiumApiUrl = formidiumApiUrl;
    }

    public String getFormidiumContext() {
        return formidiumContext;
    }

    @Inject
    public void setFormidiumContext(@Named(FORMIDIUM_CONTEXT_ROOT) String formidiumContext) {
        this.formidiumContext = formidiumContext;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
