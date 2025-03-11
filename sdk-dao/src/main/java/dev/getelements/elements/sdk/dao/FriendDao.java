package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.friend.Friend;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Provides database-level access to {@link Friend} instances.
 */
@ElementServiceExport
public interface FriendDao {

    /**
     * Fetches all {@link Friend} instances for the supplied {@link User}.
     *
     * @param user   the {@link User}
     * @param offset the offset
     * @param count  the number of results to return
     * @return a {@link Pagination<Friend>}
     */
    Pagination<Friend> getFriendsForUser(User user, int offset, int count);

    /**
     * Fetches all {@link Friend} instances for the supplied {@link User}, specifying search query.
     *
     * @param user   the {@link User}
     * @param offset the offset
     * @param count  the number of results to return
     * @param search the search query which will be used to filter the returned {@link Friend} instances
     * @return a {@link Pagination<Friend>}
     */
    Pagination<Friend> getFriendsForUser(User user, int offset, int count, String search);

    /**
     * Returns a single instance of {@link Friend} for the supplied {@link User}.  Throwing a {@link NotFoundException}
     * if the friend does not exist for the supplied {@link User}.
     *
     * @param user     the {@link User}
     * @param friendId the id of the friend as returned by {@link Friend#getId()}
     * @return the {@link Friend} instance, never null
     */
    Friend getFriendForUser(User user, String friendId);

    /**
     * Deletes the single instance of {@link Friend} for the supplied {@link User}.  Throwing a
     * {@link NotFoundException} if the friend does not exist for the supplied {@link User}.
     *
     * @param user     the {@link User}
     * @param friendId the id of the friend as returned by {@link Friend#getId()}
     */
    void deleteFriendForUser(User user, String friendId);

}
