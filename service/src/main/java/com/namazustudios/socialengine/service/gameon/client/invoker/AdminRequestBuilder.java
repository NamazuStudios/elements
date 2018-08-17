package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;

/**
 * Builds invoker instances used by the Admin API.
 *
 * @param <BuiltT>
 */
public interface AdminRequestBuilder<BuiltT> {

    /**
     * Specifies the Amazon-Assigned API Key to use.
     *
     * @param gameOnApplicationConfiguration the Gamne On configuration
     *
     * @return this instance
     */
    AdminRequestBuilder<BuiltT> withConfiguration(GameOnApplicationConfiguration gameOnApplicationConfiguration);

    /**
     * Builds the desired instance.
     *
     * @return a newly built instance
     */
    BuiltT build();

}
