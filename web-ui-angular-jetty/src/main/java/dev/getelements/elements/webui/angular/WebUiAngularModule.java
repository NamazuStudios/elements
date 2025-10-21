package dev.getelements.elements.webui.angular;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.guice.ServletBindings;
import org.eclipse.jetty.util.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

import static dev.getelements.elements.webui.angular.WebUIAngularServlet.RESOURCE_BASE;

public class WebUiAngularModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(WebUiAngularModule.class);

    private static final String WEB_UI_CONTEXT_ROOT = "old/admin/*";

    private static final Key<WebUIAngularServlet> WEB_UI_ANGULAR_SERVLET_KEY = Key.get(WebUIAngularServlet.class);

    private static final URL resourceBase = Loader.getResource(RESOURCE_BASE);

    @Override
    protected void configure() {

        if (resourceBase == null) {
            logger.warn("No UI on classpath. Did you forget to build it?");
        } else {
            expose(WEB_UI_ANGULAR_SERVLET_KEY);
            bind(WEB_UI_ANGULAR_SERVLET_KEY).asEagerSingleton();
        }

    }

    public void accept(final ServletBindings bindings) {

        final var resourceBase = Loader.getResource(RESOURCE_BASE);

        if (resourceBase != null) {
            final var params = Map.of(
                    "dirAllowed", "false",
                    "baseResource", resourceBase.toString()
            );

            logger.info("Using Resource Base {}", resourceBase);
            bindings.serve(WEB_UI_CONTEXT_ROOT).with(WEB_UI_ANGULAR_SERVLET_KEY, params);
            bindings.useGlobalAuthFor(WEB_UI_CONTEXT_ROOT);
        }

    }

}
