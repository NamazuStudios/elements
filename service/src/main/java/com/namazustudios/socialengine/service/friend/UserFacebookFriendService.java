package com.namazustudios.socialengine.service.friend;

import com.google.common.base.Joiner;
import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.service.FacebookFriendCache;
import com.namazustudios.socialengine.service.FacebookFriendService;
import com.restfb.*;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import com.restfb.types.ProfilePictureSource;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class UserFacebookFriendService implements FacebookFriendService {

    private User user;

    private FacebookUserDao facebookUserDao;

    private FacebookFriendCache facebookFriendCache;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    private static final String FIELDS_PARAMETER_VALUE = Joiner.on(",")
            .join("id","name","first_name","last_name","picture");

    @Override
    public Pagination<FacebookFriend> getUninvitedFacebookFriends(
            final String applicationNameOrId, final String applicationConfigurationNameOrId,
            final String facebookOAuthAccessToken,
            int offset, int count) {

        final FacebookApplicationConfiguration facebookApplicationConfiguration = getFacebookApplicationConfigurationDao()
             .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);

        return getFacebookFriendCache()
            .getUninvitedFriends(getUser().getId(), offset, count, () -> load(facebookOAuthAccessToken, facebookApplicationConfiguration));

    }

    private List<FacebookFriend> load(
            final String facebookOAuthAccessToken,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {
        try {
            return doLoad(facebookOAuthAccessToken, facebookApplicationConfiguration);
        }  catch (FacebookOAuthException ex) {
            throw new ForbiddenException(ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new InternalException(ex);
        }
    }

    private List<FacebookFriend> doLoad(
            final String facebookOAuthAccessToken,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final FacebookClient facebookClient;
        facebookClient = new DefaultFacebookClient(facebookOAuthAccessToken, Version.LATEST);

        final String appsecretProof = facebookClient.obtainAppSecretProof(
                facebookOAuthAccessToken,
                facebookApplicationConfiguration.getApplicationSecret());

        final com.restfb.types.User meUser = facebookClient
                .fetchObject(
                        "me",
                        com.restfb.types.User.class,
                        Parameter.with("appsecret_proof", appsecretProof));

        if (!getFacebookUserDao().findActiveByFacebookId(meUser.getId()).equals(getUser())) {
            throw new ForbiddenException("User does not match acccess token supplied.");
        }

        final Connection<com.restfb.types.User> userConnection = facebookClient
                .fetchConnection(
                        "me/friends",
                        com.restfb.types.User.class,
                        Parameter.with("fields", FIELDS_PARAMETER_VALUE),
                        Parameter.with("appsecret_proof", appsecretProof));

        final List<FacebookFriend> friendList = new ArrayList<>();

        userConnection.forEach(userList -> {

            final Map<String, User> invitedFriends = getFacebookUserDao().findActiveUsersWithFacebookIds(userList
                .stream()
                .map(u -> u.getId())
                .collect(toList()));

            userList
                .stream()
                .filter(u -> !invitedFriends.containsKey(u.getId()))
                .forEach(u -> {
                    final FacebookFriend facebookFriend = new FacebookFriend();
                    facebookFriend.setFacebookId(u.getId());
                    facebookFriend.setDisplayName(u.getFirstName() + " " + u.getLastName());
                    facebookFriend.setProfilePictureUrl(getPictureUrl(facebookClient, appsecretProof, u.getId()));
                    friendList.add(facebookFriend);
                });

        });

        return friendList;

    }

    private String getPictureUrl(final FacebookClient facebookClient, final String appsecretProof, final String id) {

        final JsonObject rawProfilePicture = facebookClient
            .fetchObject(
                format("%s/picture", id),
                JsonObject.class,
                Parameter.with("type", "large"),
                Parameter.with("redirect", false),
                Parameter.with("appsecret_proof", appsecretProof));

        final ProfilePictureSource profilePictureSource = facebookClient
                .getJsonMapper()
                .toJavaObject(rawProfilePicture.get("data").toString(), ProfilePictureSource.class);

        return profilePictureSource.getUrl();

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

    public FacebookUserDao getFacebookUserDao() {
        return facebookUserDao;
    }

    @Inject
    public void setFacebookUserDao(FacebookUserDao facebookUserDao) {
        this.facebookUserDao = facebookUserDao;
    }
}
