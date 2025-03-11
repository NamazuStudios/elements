package dev.getelements.elements.app.serve.loader;

import dev.getelements.elements.sdk.Element;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;

record DeploymentRecord(
        Element element,
        Handler handler
) {
}
