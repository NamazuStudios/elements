package com.namazustudios.socialengine.rt.guice;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.edge.EdgeFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Created by patricktwohig on 9/2/15.
 */
public abstract class EdgeFilterListModule extends AbstractModule {

    private final LinkedList<String> filterNames = new LinkedList<>();

    @Override
    protected final void configure() {
        bind(new TypeLiteral<List<EdgeFilter>>(){})
            .toProvider(new Provider<List<EdgeFilter>>() {

                @Inject
                private Injector injector;

                @Override
                public List<EdgeFilter> get() {
                    return Lists.transform(filterNames, new Function<String, EdgeFilter>() {
                        @Override
                        public EdgeFilter apply(final String input) {
                            final Key<EdgeFilter> edgeFilterKey = Key.get(EdgeFilter.class, Names.named(input));
                            return injector.getInstance(edgeFilterKey);
                        }
                    });
                }

            });

        configureFilters();
    }

    /**
     * Called to configure the filters fo the application.
     */
    protected abstract void configureFilters();

    /**
     * Binds an {@link EdgeFilter} to be added ot the server's filter chain.
     *
     * @return an instance of {@link FilterNameBindingBuilder}
     */
    protected FilterNameBindingBuilder bindEdgeFilter() {
        return new FilterNameBindingBuilder() {
            @Override
            public FilterSequenceBindingBuilder named(final String named) {
                return bindFilterNamed(named);
            }
        };
    }

    private FilterSequenceBindingBuilder bindFilterNamed(final String name) {
        return new FilterSequenceBindingBuilder() {

            @Override
            public LinkedBindingBuilder<EdgeFilter> atBeginningOfFilterChain() {

                final int index = filterNames.indexOf(name);

                if (index >= 0) {
                    throw new IllegalArgumentException("Filter named " + name + " already exists.");
                }

                return binder().bind(EdgeFilter.class)
                               .annotatedWith(Names.named(name));

            }

            @Override
            public LinkedBindingBuilder<EdgeFilter> beforeFilterNamed(final String existingFilterName) {

                final ListIterator<String> listIterator = filterNames.listIterator();

                while (listIterator.hasNext()) {

                    if (!Objects.equals(listIterator.next(), existingFilterName)) {
                        continue;
                    }

                    listIterator.add(name);

                    return binder().bind(EdgeFilter.class)
                                   .annotatedWith(Names.named(name));

                }

                throw new IllegalArgumentException("Filter does not exist " + existingFilterName);

            }

            @Override
            public LinkedBindingBuilder<EdgeFilter> afterFilterNamed(final String existingFilterName) {

                final ListIterator<String> listIterator = filterNames.listIterator();

                while (listIterator.hasNext()) {

                    final String currentFilter = listIterator.next();

                    if (!Objects.equals(currentFilter, existingFilterName)) {
                        continue;
                    }

                    listIterator.set(name);
                    listIterator.add(currentFilter);

                    return binder().bind(EdgeFilter.class)
                                   .annotatedWith(Names.named(name));

                }

                throw new IllegalArgumentException("Filter does not exist " + existingFilterName);

            }

            @Override
            public LinkedBindingBuilder<EdgeFilter> atEndOfFilterChain() {

                final int index = filterNames.indexOf(name);

                if (index >= 0) {
                    throw new IllegalArgumentException("Filter named " + name + " already exists.");
                }

                return binder().bind(EdgeFilter.class)
                               .annotatedWith(Names.named(name));

            }

        };
    }

}
