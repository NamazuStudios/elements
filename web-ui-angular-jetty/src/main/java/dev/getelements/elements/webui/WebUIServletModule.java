package dev.getelements.elements.webui;

import dev.getelements.elements.guice.BaseServletModule;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WebUIServletModule extends BaseServletModule {

    private static final Logger logger = LoggerFactory.getLogger(WebUIServletModule.class);

    @Override
    protected void configureServlets() {

        bind(WebUIAngularServlet.class).asEagerSingleton();

        final var resourceBase = Loader.getResource("dev/getelements/elements/webui/angular").toString();
        logger.info("Using Resource Base {}", resourceBase);

        final var params = Map.of("resourceBase", resourceBase);
        serve("/*").with(WebUIAngularServlet.class, params);

        useGlobalSecretOnly();

    }

}
