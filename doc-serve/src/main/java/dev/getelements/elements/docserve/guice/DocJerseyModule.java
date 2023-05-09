package dev.getelements.elements.docserve.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.docserve.DocGuiceResourceConfig;
import dev.getelements.elements.servlet.security.HttpServletSessionIdAuthenticationFilter;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Singleton;
import java.util.Map;

public class DocJerseyModule extends ServletModule {

    @Override
    protected void configureServlets() {

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);
        bind(HttpServletSessionIdAuthenticationFilter.class).asEagerSingleton();

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", DocGuiceResourceConfig.class.getName())
            .build();

        serve("/*").with(ServletContainer.class, params);

    }
}
