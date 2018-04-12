package com.namazustudios.socialengine.service.friend;

import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.service.FacebookFriendCache;
import com.namazustudios.socialengine.service.FacebookFriendService;

import javax.inject.Inject;

public class UserFacebookFriendService implements FacebookFriendService {

    private User user;

    private FacebookFriendCache facebookFriendCache;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    @Override
    public Pagination<FacebookFriend> getUninvitedFacebookFriends(
            final String applicationNameOrId, final String applicationConfigurationNameOrId,
            final String facebookOAuthAccessToken,
            int offset, int count) {

        final FacebookApplicationConfiguration facebookApplicationConfiguration = getFacebookApplicationConfigurationDao()
             .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);

        return getFacebookFriendCache().getUninvitedFriends(
                facebookApplicationConfiguration,
            facebookOAuthAccessToken,
            offset, count);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public FacebookFriendCache getFacebookFriendCache() {
        return facebookFriendCache;
    }

    @Inject
    public void setFacebookFriendCache(FacebookFriendCache facebookFriendCache) {
        this.facebookFriendCache = facebookFriendCache;
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

}
