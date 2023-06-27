package dev.getelements.elements.docserve.guice;

import com.google.common.collect.ImmutableMap;
import dev.getelements.elements.docserve.DocGuiceResourceConfig;
import dev.getelements.elements.guice.BaseServletModule;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Singleton;
import java.util.Map;

public class DocJerseyModule extends BaseServletModule {

    @Override
    protected void configureServlets() {

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", DocGuiceResourceConfig.class.getName())
            .build();

        serve("/*").with(ServletContainer.class, params);

        useStandardSecurityFilters();

    }

}
