package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;

public interface GameOnRegistrationInvoker {

    /**
     * Invokes the underlying APIs to register the {@link GameOnRegistration} with Amazon.  Throwing an exception if
     * the registration failed for any reason.
     *
     * @return the fully-registered instance
     */
    GameOnRegistration invoke();

    /**
     * A builder type used to create instance of {@link GameOnRegistrationInvoker}.
     */
    interface Builder {

        /**
         * Specifies the input {@link GameOnRegistration} to complete with the GameOn Service
         * @param gameOnRegistration
         * @return
         */
        Builder withRegistration(GameOnRegistration gameOnRegistration);

        /**
         * Specifies the {@link GameOnApplicationConfiguration} to complete with the GameOn Service.
         * @param gameOnApplicationConfiguration
         * @return
         */
        Builder withConfiguration(GameOnApplicationConfiguration gameOnApplicationConfiguration);

        /**
         * Builds the {@link GameOnRegistrationInvoker}
         *
         * @return the {@link GameOnRegistrationInvoker}
         */
        GameOnRegistrationInvoker build();

    }

}
