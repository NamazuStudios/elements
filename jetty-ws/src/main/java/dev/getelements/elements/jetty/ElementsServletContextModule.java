package dev.getelements.elements.jetty;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.deployment.jetty.loader.JakartaRsLoader;
import dev.getelements.elements.deployment.jetty.loader.JakartaWebsocketLoader;
import dev.getelements.elements.servlet.HttpContextRoot;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.guice.GuiceConstants.GUICE_INJECTOR_ATTRIBUTE_NAME;
import static java.util.EnumSet.allOf;

public class ElementsServletContextModule extends AbstractModule {

    public static final String  APP_WEBSOCKET_PREFIX = "/app/ws";

    @Override
    protected void configure() {

        final var injectorProvider = getProvider(Injector.class);
        final var guiceFilterProvider = getProvider(GuiceFilter.class);
        final var httpContextRootProvider = getProvider(HttpContextRoot.class);

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

        bind(Handler.class)
                .toProvider(() -> new Handler.Sequence(
                    jakartaRsContextHandlerProvider.get(),
                    jakartaWebsocketContextHandlerProvider.get(),
                    guiceHandlerProvider.get()
                ))
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

    }

}
