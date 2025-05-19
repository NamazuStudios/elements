package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ElementScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class is used to manage the scopes of elements in the registry.
 * It allows for the creation of a new scope with a specific selector.
 * The selector is a predicate that determines which elements are included in the scope.
 */
public class ElementScopes {

    private static final Logger defaultLogger = LoggerFactory.getLogger(ElementScopes.class);

    private final String name;

    private final Logger logger;

    private final ElementRegistry registry;

    private final Predicate<Element> selector;

    private final Supplier<Attributes> attributesSupplier;

    private ElementScopes(Builder builder) {
        this.name = builder.name;
        this.logger = builder.logger;
        this.registry = builder.registry;
        this.selector = builder.selector;
        this.attributesSupplier = builder.attributesSupplier;
    }

    /**
     * Enters the aggregate of {@link ElementScope}s which match the selector.
     *
     * @return the handle to the scope
     */
    public ElementScope.Handle enter() {

        final var aggregate = registry
                .stream()
                .filter(selector)

                .map(element -> element
                        .withScope()
                        .named(name)
                        .with(attributesSupplier.get())
                        .enter()
                )
                .map(handle -> FinallyAction
                        .begin(logger).thenClose(handle))
                .reduce(FinallyAction.begin(logger), (a, b) -> a.then(b));

        return aggregate::close;

    }

    /**
     * Creates a new builder for ElementScopes.
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for ElementScopes.
     */
    public static class Builder {

        private Logger logger = defaultLogger;

        private String name;

        private ElementRegistry registry;

        private Predicate<Element> selector = e -> true;

        private Supplier<Attributes> attributesSupplier = Attributes::emptyAttributes;

        /**
         * Specifies the name of the scope.
         * @param name the name
         * @return this builder
         */
        public Builder withName(final String name) {
            this.name = name;
            return this;
        }
        /**
         * Specifies the name of the scope, using the supplied {@link Class#getName()} as the name.
         * @param aClass name
         * @return this builder
         */
        public Builder withNameFrom(final Class<?> aClass) {
            return withName(aClass.getName());
        }

        /**
         * The logger to use.
         * @param logger the logger
         * @return the logger
         */
        public Builder withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Sets the registry to be used.
         * @param registry the registry
         * @return this builder
         */
        public Builder withRegistry(ElementRegistry registry) {
            this.registry = registry;
            return this;
        }

        /**
         * Sets the selector to be used.
         * @param selector the selector
         * @return this builder
         */
        public Builder withSelector(Predicate<Element> selector) {

            if (selector == null) {
                throw new IllegalStateException("Selector must not be null");
            }

            this.selector = selector;
            return this;

        }

        /**
         * Sets the selector to include elements with the specified names.
         * @param names the names of the elements
         * @return this builder
         */
        public Builder withElementsNamed(String ... names) {
            final var namesSet = Set.of(names);
            this.selector = e -> namesSet.contains(e.getElementRecord().definition().name());
            return this;
        }

        /**
         * Sets the {@link Attributes} to assign to the scope.
         *
         * @param attributes the attributes to assign
         * @return this builder
         */
        public Builder withAttributes(final Attributes attributes) {
            return withAttributesSupplier(() -> attributes);
        }

        /**
         * Sets the {@link Attributes} {@link Supplier} to assign to the scope.
         * @param attributesSupplier the attributes supplier
         * @return this builder
         */
        private Builder withAttributesSupplier(final Supplier<Attributes> attributesSupplier) {
            this.attributesSupplier = attributesSupplier;
            return this;
        }

        /**
         * Creates a new instance of {@link ElementScopes} with the specified parameters.
         * @return the new instance
         */
        public ElementScopes build() {

            if (registry == null) {
                throw new IllegalStateException("Registry must not be null");
            }

            return new ElementScopes(this);

        }

    }
}