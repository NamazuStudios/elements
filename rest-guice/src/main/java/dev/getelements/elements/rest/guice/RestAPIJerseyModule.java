package dev.getelements.elements.rest.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.opencsv.CSVWriter;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.MappingStrategy;
import dev.getelements.elements.guice.ServletBindings;
import dev.getelements.elements.sdk.jakarta.rs.MethodOverrideFilter;
import dev.getelements.elements.rest.status.VersionResource;
import dev.getelements.elements.sdk.jakarta.rs.DefaultExceptionMapper;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.Writer;
import java.util.Map;
import java.util.function.Function;

import static com.google.inject.name.Names.named;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class RestAPIJerseyModule extends PrivateModule {

    public static final String REST_API_ROOT = "/api/rest/*";

    public static final String SERVLET_NAME = "dev.getelements.elements.rest.servlet";

    public static final Key<ServletContainer> SERVLET_KEY = Key.get(ServletContainer.class, named(SERVLET_NAME));

    @Override
    protected final void configure() {

        // Binds specific resources
        bind(VersionResource.class);
        bind(SwaggerSerializers.class);
        bind(MethodOverrideFilter.class);
        bind(DefaultExceptionMapper.class);

        bind(RestDocRedirectFilter.class).asEagerSingleton();

        // Binds writers
        bind(new TypeLiteral<Function<Writer, CSVWriter>>(){}).toInstance(CSVWriter::new);
        bind(new TypeLiteral<MappingStrategy<Object>>(){}).toProvider(HeaderColumnNameMappingStrategy::new);

        // Binds the servlets
        bind(SERVLET_KEY).to(ServletContainer.class).asEagerSingleton();

        // Exposes what is needed for the rest of the application.
        expose(SERVLET_KEY);
        expose(RestDocRedirectFilter.class);
        expose(new TypeLiteral<MappingStrategy<Object>>(){});
        expose(new TypeLiteral<Function<Writer, CSVWriter>>(){});

    }

    public void accept(final ServletBindings bindings) {

        final var servletParams = Map.of(
                "jakarta.ws.rs.Application",
                RestAPIGuiceResourceConfig.class.getName()
        );

        bindings.serve(REST_API_ROOT).with(SERVLET_KEY, servletParams);
        bindings.useGlobalAuthFor(REST_API_ROOT);
        bindings.useStandardAuthFor(REST_API_ROOT);

    }

}
