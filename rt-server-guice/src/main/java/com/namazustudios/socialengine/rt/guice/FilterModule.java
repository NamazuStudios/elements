package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.handler.Filter;

import java.io.File;

/**
 * Allows for the configuration of {@link Filter}s for servicing {@link Request} instances.
 *
 * Created by patricktwohig on 9/2/15.
 */
public class FilterModule extends PrivateModule {

    private Runnable exposeRequestAction = () -> {};

    private Runnable exposeFilterChainBuilderAction = () -> {};

    private Multibinder<Filter> filterMultibinder;

    @Override
    protected final void configure() {

        bind(Filter.Chain.Builder.class).to(GuiceInjectorFilterChainBuilder.class);

        // Ensures that the scope
        RequestScope.bind(binder());

        filterMultibinder = Multibinder.newSetBinder(binder(), Filter.class);
        configureFilters();

        exposeRequestAction.run();
        expose(Filter.Chain.Builder.class);

    }

    /**
     * Exposes the {@link Request} binding.
     *
     * @return this instance
     */
    public FilterModule exposingRequest() {
        exposeRequestAction = () -> expose(Request.class);
        return this;
    }

    /**
     * Called to configure the filters for the application.  Leave as-is to install no filters.
     */
    protected void configureFilters() {};

    /**
     * Binds an {@link Filter} to be added ot the server's filter chain.
     *
     * @return an instance of {@link FilterNameBindingBuilder}
     */
    protected LinkedBindingBuilder<Filter> bindFilter() {
        return filterMultibinder.addBinding();
    }

}
