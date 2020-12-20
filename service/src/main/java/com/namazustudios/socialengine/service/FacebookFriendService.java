package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Provides access to {@link FacebookFriend} instances.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.scoped.facebook.friend"),
    @ExposedModuleDefinition(value = "namazu.elements.service.unscoped.facebook.friend", annotation = Unscoped.class)
})
public interface FacebookFriendService {

    /**
     * Given the application name, configuration, Facebook OAuth Token, this will cross-check the user's friends against
     * the Facebook API to determine which of his or her friends have not been invited to the application.
     *
     * @param applicationNameOrId the {@link com.namazustudios.socialengine.model.application.Application} name or id
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
