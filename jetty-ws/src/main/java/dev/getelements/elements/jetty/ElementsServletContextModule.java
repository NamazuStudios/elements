package dev.getelements.elements.jetty;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.deployment.jetty.loader.HttpPathRegistry;
import dev.getelements.elements.deployment.jetty.loader.JakartaRsLoader;
import dev.getelements.elements.deployment.jetty.loader.JakartaWebsocketLoader;
import dev.getelements.elements.deployment.jetty.loader.StaticContentLoader;
import dev.getelements.elements.servlet.HttpContextRoot;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;

import java.util.List;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.guice.GuiceConstants.GUICE_INJECTOR_ATTRIBUTE_NAME;
import static java.util.EnumSet.allOf;

public class ElementsServletContextModule extends AbstractModule {

    public static final String APP_WEBSOCKET_PREFIX = "/app/ws";

    /**
     * System API paths served by the Guice filter. These are pre-seeded into the {@link HttpPathRegistry} at
     * startup so that catch-all static content handlers (e.g. deployed at {@code /}) skip them.
     */
    private static final List<String> SYSTEM_API_PATHS = List.of(
            "/api/rest",
            "/cdn/git",
            "/cdn/object",
            "/cdn/static/app",
            "/admin",
            "/doc/swagger"
    );

    @Override
    protected void configure() {

        final var injectorProvider = getProvider(Injector.class);
        final var guiceFilterProvider = getProvider(GuiceFilter.class);
        final var httpContextRootProvider = getProvider(HttpContextRoot.class);
        final var httpPathRegistryProvider = getProvider(HttpPathRegistry.class);

        final var guiceHandlerProvider = getProvider(Key.get(
                ServletContextHandler.class,
                named(ElementsWebServices.MAIN_HANDLER)
        ));

        final var jakartaRsContextHandlerProvider = getProvider(Key.get(
                Handler.Sequence.class,
                named(JakartaRsLoader.HANDLER_SEQUENCE)
        ));

        final var jakartaWebsocketContextHandlerProvider = getProvider(Key.get(
                Handler.Sequence.class,
                named(JakartaWebsocketLoader.HANDLER_SEQUENCE)
        ));

        final var staticContentContextHandlerProvider = getProvider(Key.get(
                Handler.Sequence.class,
                named(StaticContentLoader.HANDLER_SEQUENCE)
        ));

        bind(Handler.class)
                .toProvider(() -> {
                    final var httpContextRoot = httpContextRootProvider.get();
                    final var registry = httpPathRegistryProvider.get();
                    for (final var rawPath : SYSTEM_API_PATHS) {
                        registry.register(httpContextRoot.normalize(rawPath));
                    }
                    return new Handler.Sequence(
                        jakartaRsContextHandlerProvider.get(),
                        jakartaWebsocketContextHandlerProvider.get(),
                        staticContentContextHandlerProvider.get(),
                        guiceHandlerProvider.get()
                    );
                })
                .asEagerSingleton();

        requireBinding(GuiceFilter.class);
        requireBinding(HttpContextRoot.class);

        bind(ServletContextHandler.class)
                .annotatedWith(named(ElementsWebServices.MAIN_HANDLER))
                .toProvider(() -> {
                    final var servletContextHandler = new ServletContextHandler();
                    servletContextHandler.setAttribute(GUICE_INJECTOR_ATTRIBUTE_NAME, injectorProvider.get());
                    servletContextHandler.addFilter(guiceFilterProvider.get(), "/*", allOf(DispatcherType.class));
                    servletContextHandler.setContextPath(httpContextRootProvider.get().getHttpPathPrefix());
                    return servletContextHandler;
                }).asEagerSingleton();

        bind(Handler.Sequence.class)
                .annotatedWith(named(JakartaRsLoader.HANDLER_SEQUENCE))
                .toProvider(Handler.Sequence::new)
                .asEagerSingleton();

        bind(Handler.Sequence.class)
                .annotatedWith(named(JakartaWebsocketLoader.HANDLER_SEQUENCE))
                .toProvider(Handler.Sequence::new)
                .asEagerSingleton();

        bind(Handler.Sequence.class)
                .annotatedWith(named(StaticContentLoader.HANDLER_SEQUENCE))
                .toProvider(Handler.Sequence::new)
                .asEagerSingleton();

    }

}
