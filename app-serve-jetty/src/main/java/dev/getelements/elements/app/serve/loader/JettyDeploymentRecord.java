package dev.getelements.elements.app.serve.loader;

import dev.getelements.elements.sdk.Element;
import org.eclipse.jetty.server.Handler;

record JettyDeploymentRecord(
        Element element,
        Handler handler
) {
}
