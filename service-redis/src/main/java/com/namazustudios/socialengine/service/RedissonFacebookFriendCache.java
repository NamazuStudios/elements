package com.namazustudios.socialengine.service;

import com.google.common.base.Joiner;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.restfb.*;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import com.restfb.types.ProfilePictureSource;
import org.redisson.api.RListMultimapCache;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class RedissonFacebookFriendCache implements FacebookFriendCache {

    public static final String CACHE_NAME = "uninvited_facebook_friends";

    private static final String FIELDS_PARAMETER_VALUE = Joiner.on(",")
            .join("id","name","first_name","last_name","picture");

    private User user;

    private int queryMaxResults;

    private FacebookUserDao facebookUserDao;

    private RListMultimapCache<String, FacebookFriend> uninvitedFriendCache;

    @Override
    public Pagination<FacebookFriend> getUninvitedFriends(
            final FacebookApplicationConfiguration facebookApplicationConfiguration,
            final String facebookOAuthAccessToken,
            final int offset, final int count) {

        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }

        if (count < 0) {
            throw new IllegalArgumentException("count must be positive");
        } if (count == 0) {
            return Pagination.empty();
        }

        final List<FacebookFriend> facebookFriends = getUninvitedFriendCache().get(getUser().getId());

        if (facebookFriends.isEmpty()) {
            fillCache(facebookOAuthAccessToken, facebookApplicationConfiguration);
        }

        final Pagination<FacebookFriend> facebookFriendPagination = new Pagination<>();
        final int fromIndex = min(max(0, facebookFriends.size() - 1), offset);
        final int toIndex = min(max(0, facebookFriends.size()), count + offset);
        final List<FacebookFriend> facebookFriendsSubList = facebookFriends.subList(fromIndex, toIndex);

        facebookFriendPagination.setOffset(fromIndex);
        facebookFriendPagination.setObjects(new ArrayList<>(facebookFriendsSubList));
        facebookFriendPagination.setTotal(facebookFriends.size());

        return facebookFriendPagination;

    }

    private List<FacebookFriend> fillCache(
            final String facebookOAuthAccessToken,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {
        try {
            return doFillCache(facebookOAuthAccessToken, facebookApplicationConfiguration);
        }  catch (FacebookOAuthException ex) {
            throw new ForbiddenException(ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new InternalException(ex);
        }
    }

    private List<FacebookFriend> doFillCache(
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

        getUninvitedFriendCache().replaceValues(getUser().getId(), friendList);

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

    public FacebookUserDao getFacebookUserDao() {
        return facebookUserDao;
    }

    @Inject
    public void setFacebookUserDao(FacebookUserDao facebookUserDao) {
        this.facebookUserDao = facebookUserDao;
    }

    public RListMultimapCache<String, FacebookFriend> getUninvitedFriendCache() {
        return uninvitedFriendCache;
    }

    @Inject
    public void setUninvitedFriendCache(@Named(CACHE_NAME) RListMultimapCache<String, FacebookFriend> uninvitedFriendCache) {
        this.uninvitedFriendCache = uninvitedFriendCache;
    }
    public int getQueryMaxResults() {
        return queryMaxResults;
    }

    @Inject
    public void setQueryMaxResults(    @Named(Constants.QUERY_MAX_RESULTS) int queryMaxResults) {
        this.queryMaxResults = queryMaxResults;
    }

}
