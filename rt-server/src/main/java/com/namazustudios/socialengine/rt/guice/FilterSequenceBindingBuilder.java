package com.namazustudios.socialengine.rt.guice;

import com.google.inject.binder.ScopedBindingBuilder;

/**
 * Created by patricktwohig on 9/2/15.
 */
public interface FilterSequenceBindingBuilder {

    /**
     * Prepends the filter athe beginning of the filter chain.
     *
     * @return a {@link ScopedBindingBuilder}
     */
    ScopedBindingBuilder atBeginningOfFilterChain();

    /**
     * Appends the filter before the filter with the given name.
     *
     * @param filterName the name of the filter
     *
     * @return a {@link ScopedBindingBuilder}
     */
    ScopedBindingBuilder beforeFilterNamed(String filterName);

    /**
     * Appends the filter after the filter with the given name.
     *
     * @param filterName the name of the filter
     *
     * @return a {@link ScopedBindingBuilder}
     */
    ScopedBindingBuilder afterFilterNamed(String filterName);

    /**
     * Appends the filter to the end of the filter chain.
     *
     * @return a {@link ScopedBindingBuilder}
     */
    ScopedBindingBuilder atEndOfFilterChain();

}
