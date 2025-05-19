package dev.getelements.elements.docserve;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.guice.ServletBindings;
import dev.getelements.elements.jetty.StaticContentServlet;

import java.util.Map;

import static com.google.inject.name.Names.named;
import static org.eclipse.jetty.util.Loader.getResource;

public class DocModule extends PrivateModule {

    public static final String DOC_SWAGGER_ROOT = "/doc/swagger/*";

    public static final String SERVLET_NAME = "dev.getelements.elements.doc.swagger.servlet";

    public static final Key<StaticContentServlet> SERVLET_KEY = Key.get(StaticContentServlet.class, named(SERVLET_NAME));

    @Override
    protected void configure() {

        bind(SERVLET_KEY).to(StaticContentServlet.class).asEagerSingleton();
        bind(DocSwaggerConfigurationFilter.class).asEagerSingleton();

        expose(SERVLET_KEY);
        expose(DocSwaggerConfigurationFilter.class);

    }

    public void accept(final ServletBindings bindings) {

        final var servletParameters = Map.of(
                "dirAllowed", "false",
                "baseResource", getResource("swagger").toString()
        );

        bindings.filter(DOC_SWAGGER_ROOT).through(DocSwaggerConfigurationFilter.class);

        bindings.useGlobalAuthFor(DOC_SWAGGER_ROOT);
        bindings.serve(DOC_SWAGGER_ROOT).with(SERVLET_KEY, servletParameters);

    }

}
