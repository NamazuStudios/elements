package com.namazustudios.socialengine.rt.guice;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.namazustudios.socialengine.rt.handler.Filter;

/**
 * Created by patricktwohig on 9/2/15.
 */
public interface FilterSequenceBindingBuilder {

    /**
     * Prepends the filter athe beginning of the filter chain.
     *
     * @return a {@link ScopedBindingBuilder}
     */
    LinkedBindingBuilder<Filter> atBeginningOfFilterChain();

    /**
     * Appends the filter before the filter with the given name.
     *
     * @param filterName the name of the filter
     *
     * @return a {@link ScopedBindingBuilder}
     */
    LinkedBindingBuilder<Filter> beforeFilterNamed(String filterName);

    /**
     * Appends the filter after the filter with the given name.
     *
     * @param filterName the name of the filter
     *
     * @return a {@link ScopedBindingBuilder}
     */
    LinkedBindingBuilder<Filter> afterFilterNamed(String filterName);

    /**
     * Appends the filter to the end of the filter chain.
     *
     * @return a {@link ScopedBindingBuilder}
     */
    LinkedBindingBuilder<Filter> atEndOfFilterChain();

}
