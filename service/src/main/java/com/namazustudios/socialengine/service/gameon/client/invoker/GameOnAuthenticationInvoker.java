package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.model.gameon.GameOnSession;

public interface GameOnAuthenticationInvoker {

    /**
     * Invokes the underlying API to obtain the {@link GameOnSession}.
     *
     * @return the fully authenticated {@link GameOnSession}, throwing an exception if something failed.
     */
    GameOnSession invoke();

    /**
     * A builder type used to create instance of {@link GameOnRegistrationInvoker}.
     */
    interface Builder {

        /**
         * Specifies the input {@link GameOnRegistration} to complete with the GameOn Service
         *
         * @param gameOnSession the partial {@link GameOnSession} instance
         * @return this instance
         */
        Builder withSession(GameOnSession gameOnSession);

        /**
         * Specifies the {@link GameOnRegistration} which will be used to authenticate the user.
         *
         * @param gameOnRegistration the {@link GameOnRegistration}
         * @return this instance
         */
        Builder withRegistration(GameOnRegistration gameOnRegistration);

        /**
         * Specifies the {@link GameOnApplicationConfiguration} to complete with the GameOn Service.
         *
         * @param gameOnApplicationConfiguration
         * @return this instance
         */
        Builder withConfiguration(GameOnApplicationConfiguration gameOnApplicationConfiguration);

        /**
         * Builds the instance of {@link GameOnAuthenticationInvoker}.
         *
         * @return the newly created {@link GameOnAuthenticationInvoker}
         */
        GameOnAuthenticationInvoker build();

    }

}
