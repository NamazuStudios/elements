package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;

@FunctionalInterface
public interface GameOnRegistrationInvoker {

    /**
     * Invokes the underlying APIs to register the {@link GameOnRegistration} with Amazon.
     *
     * @return the fully-registered instance
     */
    GameOnRegistration invoke();

    interface Builder {

        Builder withRegistration(final GameOnRegistration gameOnRegistration);

        Builder withConfiguration(final GameOnApplicationConfiguration gameOnApplicationConfiguration);

        GameOnRegistrationInvoker build();

    }

}
