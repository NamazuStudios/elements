package dev.getelements.elements.webui.react;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.guice.ServletBindings;
import org.eclipse.jetty.util.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

import static dev.getelements.elements.webui.react.WebUIReactServlet.RESOURCE_BASE;

public class WebUiReactModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(WebUiReactModule.class);

    private static final String WEB_UI_CONTEXT_ROOT = "/admin/*";

    private static final Key<WebUIReactServlet> WEB_UI_REACT_SERVLET_KEY = Key.get(WebUIReactServlet.class);

    private static final URL resourceBase = Loader.getResource(RESOURCE_BASE);

    @Override
    protected void configure() {

        if (resourceBase == null) {
            logger.warn("No UI on classpath. Did you forget to build it?");
        } else {
            expose(WEB_UI_REACT_SERVLET_KEY);
            bind(WEB_UI_REACT_SERVLET_KEY).asEagerSingleton();
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
            bindings.serve(WEB_UI_CONTEXT_ROOT).with(WEB_UI_REACT_SERVLET_KEY, params);
            bindings.useGlobalAuthFor(WEB_UI_CONTEXT_ROOT);
        }

    }

}
