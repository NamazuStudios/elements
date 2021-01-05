package com.namazustudios.socialengine.rt.guice;

import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.CurrentRequest;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class GuiceInjectorFilterChainBuilder implements Filter.Chain.Builder {

    private static final Logger logger = getLogger(GuiceInjectorFilterChainBuilder.class);

    private final Injector injector;

    private final Provider<Set<Filter>> configuredFilterListProvider;

    private final List<Supplier<Filter>> filterSupplierList = new ArrayList<>();

    @Inject
    public GuiceInjectorFilterChainBuilder(final Injector injector,
                                           final Provider<Set<Filter>> configuredFilterListProvider) {
        this.injector = injector;
        this.configuredFilterListProvider = configuredFilterListProvider;
    }

    @Override
    public Filter.Chain.Builder withFilter(final Filter filter) {
        filterSupplierList.add(() -> {
            injector.injectMembers(filter);
            return filter;
        });
        return this;
    }

    @Override
    public Filter.Chain.Builder withFilters(final Iterable<Filter> filters) {
        filters.forEach(this::withFilter);
        return this;
    }

    @Override
    public Filter.Chain terminate(final Filter.Chain terminal) {
        return new GuiceScopedChain(terminal);
    }

    private class GuiceScopedChain implements Filter.Chain {

        private final Filter.Chain terminal;

        public GuiceScopedChain(final Filter.Chain terminal) {
            if (terminal == null) throw new IllegalArgumentException();
            this.terminal = terminal;
        }

        @Override
        public void next(final Session session,
                         final Request request,
                         final Consumer<Response> responseReceiver) {

            CurrentRequest.getInstance().ensureEmpty();

            if (request == null) throw new IllegalArgumentException();
            if (session == null) throw new IllegalArgumentException();
            if (responseReceiver == null) throw new IllegalStateException();

            try (var outer = CurrentRequest.getInstance().enter(request)) {

                final Stream<Filter> configured = configuredFilterListProvider.get().stream();
                final Stream<Filter> additional = filterSupplierList.stream().map(Supplier::get);
                final List<Filter> filters = Stream.concat(configured, additional)
                    .map(ScopeFilterWrapper::new)
                    .collect(Collectors.toList());

                final Consumer<Response> scopedResponseConsumer = response -> {
                    try (var inner = CurrentRequest.getInstance().enter(request)) {
                        responseReceiver.accept(response);
                    }
                };

                // At the beginning of the chain, we should make sure that we have no scope set.
                Filter.Chain.build(filters, terminal).next(session, request, scopedResponseConsumer);

            } catch (Exception ex) {
                logger.error("Caught exception in filter chain builder.", ex);
                throw ex;
            }

            // We should also make sure that after the call, the scope is empty and all intermediate states have
            // been cleared.
            CurrentRequest.getInstance().ensureEmpty();

        }

    }

    private class ScopeFilterWrapper implements Filter {

        private final Filter delegate;

        public ScopeFilterWrapper(Filter delegate) {
            this.delegate = delegate;
        }

        @Override
        public void filter(final Chain next,
                           final Session session,
                           final Request request,
                           final Consumer<Response> responseReceiver) {
            try (var context = CurrentRequest.getInstance().enter(request)) {
                delegate.filter(next, session, request, responseReceiver);
            }
        }

    }

}
