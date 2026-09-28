package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Element;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;

record JettyDeploymentRecord(
        String deploymentId,
        Element element,
        Handler handler
) {

    String contextPath() {
        return handler() instanceof final ServletContextHandler sch ? sch.getContextPath() : null;
    }

}