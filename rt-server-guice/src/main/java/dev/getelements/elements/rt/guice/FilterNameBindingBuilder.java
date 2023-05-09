package dev.getelements.elements.rt.guice;

import javax.inject.Named;

/**
 * Created by patricktwohig on 9/2/15.
 */
public interface FilterNameBindingBuilder {

    /**
     * Names the filter.  The filter is bound using the {@link Named} annotation.
     *
     * @param named the name
     * @return a {@link FilterSequenceBindingBuilder}
     */
    FilterSequenceBindingBuilder named(String named);

}
