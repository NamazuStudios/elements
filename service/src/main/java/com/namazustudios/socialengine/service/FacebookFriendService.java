package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.friend.FacebookFriend;

public interface FacebookFriendService {

    Pagination<FacebookFriend> getUnivitedFacebookFriends(String applicationNameOrId, String applicationConfigurationNameOrId, String facebookOAuthAccessToken, int offset, int count);

}
