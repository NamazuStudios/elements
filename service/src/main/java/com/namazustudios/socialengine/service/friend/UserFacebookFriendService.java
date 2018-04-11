package com.namazustudios.socialengine.service.friend;

import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.FacebookFriendDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.FacebookFriendService;
import com.restfb.*;

import javax.inject.Inject;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class UserFacebookFriendService implements FacebookFriendService {

    private User user;

    private FacebookFriendDao facebookFriendDao;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    @Override
    public Pagination<FacebookFriend> getUnivitedFacebookFriends(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String facebookOAuthAccessToken,
            final int offset, final int count) {

        final FacebookClient facebookClient = new DefaultFacebookClient(facebookOAuthAccessToken, Version.LATEST);

        final FacebookApplicationConfiguration facebookApplicationConfiguration = getFacebookApplicationConfigurationDao()
                .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);

        final String appsecretProof = facebookClient.obtainAppSecretProof(
                facebookOAuthAccessToken,
                facebookApplicationConfiguration.getApplicationSecret());

        final Connection<com.restfb.types.User> userConnection = facebookClient
                .fetchConnection(
                    "me/friends",
                    com.restfb.types.User.class,
                    Parameter.with("appsecret_proof", appsecretProof));

        for (final List<com.restfb.types.User> userList : userConnection) {
            getFacebookFriendDao().associateFriends(user, userList.stream().map(u -> u.getId()).collect(toList()));
        }

        final List<String> friendFacebookUserIds = emptyList();
        return getFacebookFriendDao().getUninvitedFriends(getUser(), friendFacebookUserIds);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public FacebookFriendDao getFacebookFriendDao() {
        return facebookFriendDao;
    }

    @Inject
    public void setFacebookFriendDao(FacebookFriendDao facebookFriendDao) {
        this.facebookFriendDao = facebookFriendDao;
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

}
