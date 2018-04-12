package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.friend.FacebookFriend;

public interface FacebookFriendCache {

    Pagination<FacebookFriend> getUninvitedFriends(
            FacebookApplicationConfiguration facebookApplicationConfiguration,
            String facebookOAuthAccessToken,
            int offset, int count);

}
