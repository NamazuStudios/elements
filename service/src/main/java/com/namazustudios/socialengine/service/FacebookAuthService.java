package com.namazustudios.socialengine.service;

import com.google.common.base.Joiner;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.session.FacebookSessionCreation;

/**
 * Used to authorize requests users using Facebook OAuth access tokens.  This acts as the connection
 * point between.
 *
 * Created by patricktwohig on 6/22/17.
 */
public interface FacebookAuthService {

    /**
     * Creates a new session using the supplied application configuration as well as the Facebook token.  If the user
     * does not exist, then this may create a user in the system.  If creating a user is not possible then this may
     * throw the appropraite exception indicating so.  The thrown exception should be as descriptive as necessary and
     * must render the appropriate status code.
     *
     * In addition to manipulating the user account, this should also convert the short-term facebook token
     * to a long-term token and supply the result.
     *
     * @param applicationNameOrId the application name or id
     * @param applicationConfigurationNameOrId the application configuration name or id
     * @param facebookOAuthAccessToken the facebook token the facebook token
     *
     * @return
     */
    FacebookSessionCreation createOrUpdateUserWithFacebookOAuthAccessToken(String applicationNameOrId,
                                                                           String applicationConfigurationNameOrId,
                                                                           String facebookOAuthAccessToken);

}
