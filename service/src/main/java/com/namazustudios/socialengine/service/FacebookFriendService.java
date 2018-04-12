package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.friend.FacebookFriend;

import java.util.List;

public interface FacebookFriendService {

    Pagination<FacebookFriend> getUninvitedFacebookFriends(
        String applicationNameOrId, String applicationConfigurationNameOrId, String facebookOAuthAccessToken,
        int offset, int count);

}
