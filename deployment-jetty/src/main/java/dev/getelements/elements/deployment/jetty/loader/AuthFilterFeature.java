package dev.getelements.elements.deployment.jetty.loader;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;

import java.util.Set;
import java.util.function.Consumer;

import static java.util.EnumSet.allOf;

public class AuthFilterFeature implements Consumer<ServletContextHandler> {

    public static final String FILTER_SET = "dev.getelements.elements.app.serve.loader.auth.filter.set";

    private Provider<Set<Filter>> filterSetProvider;

    @Override
    public void accept(final ServletContextHandler servletContextHandler) {
        getFilterSetProvider()
                .get()
                .forEach(f -> servletContextHandler.addFilter(f, "/*", allOf(DispatcherType.class)));
    }

    public Provider<Set<Filter>> getFilterSetProvider() {
        return filterSetProvider;
    }

    @Inject
    public void setFilterSetProvider(@Named(FILTER_SET) Provider<Set<Filter>> filterSetProvider) {
        this.filterSetProvider = filterSetProvider;
    }

}
