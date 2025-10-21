    package dev.getelements.elements.jetty;

    import com.google.inject.servlet.ServletModule;
    import dev.getelements.elements.docserve.DocModule;
    import dev.getelements.elements.guice.ServletBindings;
    import dev.getelements.elements.rest.guice.RestAPIJerseyModule;
    import dev.getelements.elements.sdk.model.user.User;
    import dev.getelements.elements.servlet.security.*;
    import dev.getelements.elements.webui.angular.WebUiAngularModule;
    import dev.getelements.elements.webui.react.WebUiReactModule;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import java.util.EnumSet;
    import java.util.Objects;
    import java.util.Set;
    import java.util.stream.Collectors;

    import static dev.getelements.elements.jetty.ElementsWebServiceComponent.*;

    public class ElementsServletModule extends ServletModule {

        private static final Logger logger = LoggerFactory.getLogger(ElementsServletModule.class);

        private final Set<ElementsWebServiceComponent> components;

        private final ServletBindings bindings = new ServletBindings() {

            @Override
            public void useBasicAuthFor(final String urlPattern) {
                filter(urlPattern).through(HttpServletBasicAuthFilter.class);
            }

            @Override
            public void useGlobalAuthFor(final String urlPattern) {
                ElementsServletModule.this.filter(urlPattern).through(HttpServletGlobalSecretHeaderFilter.class);
            }

            @Override
            public void useStandardAuthFor(final String urlPattern) {
                ElementsServletModule.this.filter(urlPattern).through(HttpServletBearerAuthenticationFilter.class);
                ElementsServletModule.this.filter(urlPattern).through(HttpServletSessionIdAuthenticationFilter.class);
                ElementsServletModule.this.filter(urlPattern).through(HttpServletHeaderProfileOverrideFilter.class);
            }

            @Override
            public FilterKeyBindingBuilder filter(final String urlPattern, final String... morePatterns) {
                return ElementsServletModule.this.filter(urlPattern, morePatterns);
            }

            @Override
            public FilterKeyBindingBuilder filter(final Iterable<String> urlPatterns) {
                return ElementsServletModule.this.filter(urlPatterns);
            }

            @Override
            public FilterKeyBindingBuilder filterRegex(final String regex, final String... regexes) {
                return ElementsServletModule.this.filterRegex(regex, regexes);
            }

            @Override
            public FilterKeyBindingBuilder filterRegex(final Iterable<String> regexes) {
                return ElementsServletModule.this.filterRegex(regexes);
            }

            @Override
            public ServletKeyBindingBuilder serve(final String urlPattern, final String... morePatterns) {
                return ElementsServletModule.this.serve(urlPattern, morePatterns);
            }

            @Override
            public ServletKeyBindingBuilder serve(final Iterable<String> urlPatterns) {
                return ElementsServletModule.this.serve(urlPatterns);
            }

            @Override
            public ServletKeyBindingBuilder serveRegex(final String regex, final String... regexes) {
                return ElementsServletModule.this.serveRegex(regex, regexes);
            }

            @Override
            public ServletKeyBindingBuilder serveRegex(final Iterable<String> regexes) {
                return ElementsServletModule.this.serveRegex(regexes);
            }

        };

        public ElementsServletModule() {
            this.components = EnumSet.allOf(ElementsWebServiceComponent.class);
        }

        public ElementsServletModule(final Iterable<ElementsWebServiceComponent> components) {
            this.components = EnumSet.noneOf(ElementsWebServiceComponent.class);
            components.forEach(this.components::add);
        }

        @Override
        protected void configureServlets() {

            install(new ElementsCoreFilterModule());

            bind(User.class).toProvider(HttpRequestAttributeUserProvider.class);

            filter("/*").through(HttpServletCORSFilter.class);
            filter("/*").through(HttpServletElementScopeFilter.class);

            final var components = EnumSet.copyOf(this.components);
            components.remove(app_node);
            components.remove(app_serve);

            if (components.remove(api)) {
                final var module = new RestAPIJerseyModule();
                install(module);
                module.accept(bindings);
            }

            if (components.remove(doc)) {
                final var module = new DocModule();
                install(module);
                module.accept(bindings);
            }

            if (components.remove(cdn)) {
                final var module = new CdnServeModule();
                install(module);
                module.accept(bindings);
            }

            if (components.remove(code)) {
                final var module = new CodeServeModule();
                install(module);
                module.accept(bindings);
            }

            if (components.remove(web_ui_old)) {
                final var module = new WebUiAngularModule();
                install(module);
                module.accept(bindings);
            }

            if (components.remove(web_ui)) {
                final var module = new WebUiReactModule();
                install(module);
                module.accept(bindings);
            }

            if (components.isEmpty()) {
                logger.info("Launched all components: {}", this.components
                        .stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(","))
                );
            } else {
                logger.warn("Components not launched (not supported): {}", components
                        .stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(","))
                );
            }

        }

    }
