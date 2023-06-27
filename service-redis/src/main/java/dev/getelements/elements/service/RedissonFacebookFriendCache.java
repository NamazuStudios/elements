package dev.getelements.elements.service;

import dev.getelements.elements.Constants;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.friend.FacebookFriend;
import org.redisson.api.RListMultimapCache;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RedissonFacebookFriendCache implements FacebookFriendCache {

    public static final String CACHE_NAME = "uninvited_facebook_friends";

    private int queryMaxResults;

    private RListMultimapCache<String, FacebookFriend> uninvitedFriendCache;

    @Override
    public Pagination<FacebookFriend> getUninvitedFriends(
            final String key, final int offset, final int count,
            final Supplier<Iterable<FacebookFriend>> loader) {

        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }

        if (count < 0) {
            throw new IllegalArgumentException("count must be positive");
        } if (count == 0) {
            return Pagination.empty();
        }

        final List<FacebookFriend> facebookFriends = getUninvitedFriendCache().get(key);

        if (facebookFriends.isEmpty()) {
            getUninvitedFriendCache().replaceValues(key, loader.get());
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
