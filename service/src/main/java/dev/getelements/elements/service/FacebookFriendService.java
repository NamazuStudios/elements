package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.model.friend.FacebookFriend;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Provides access to {@link FacebookFriend} instances.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.facebook.friend")
})
public interface FacebookFriendService {

    /**
     * Given the application name, configuration, Facebook OAuth Token, this will cross-check the user's friends against
     * the Facebook API to determine which of his or her friends have not been invited to the application.
     *
     * @param applicationNameOrId the {@link dev.getelements.elements.model.application.Application} name or id
     * @param applicationConfigurationNameOrId the {@link FacebookApplicationConfiguration} name or id
     * @param facebookOAuthAccessToken the facebook issued OAuth token
     * @param offset the offset in the result set
     * @param count the number of results to return in the page
     * @return the {@link Pagination<FacebookFriend>}
     */
    Pagination<FacebookFriend> getUninvitedFacebookFriends(
        String applicationNameOrId, String applicationConfigurationNameOrId, String facebookOAuthAccessToken,
        int offset, int count);

}
