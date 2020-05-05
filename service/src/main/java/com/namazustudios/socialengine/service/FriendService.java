package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.friend.Friend;

/**
 * Allows access to instances of {@link Friend}.  This is responsible for determining the current {@link User} as well
 * and ensuring that the associated {@link Friend} instances are properly filtered and represented.
 */
public interface FriendService {

    /**
     * Gets the listing of {@link Friend} with the supplied offset and count.
     *
     * @param offset the offset
     * @param count the count
     * @return the list of {@link Friend} instances
     */
    Pagination<Friend> getFriends(int offset, int count);

    /**
     * Gets the listing of {@link Friend} with the supplied offset, count, and search query.
     *
     * @param offset the offset
     * @param count the count
     * @param search the search query
     * @return the list of {@link Friend} instances
     */
    Pagination<Friend> getFriends(int offset, int count, String search);

    /**
     * Gets a single instance of {@link Friend}.  Throws an exception if the supplied {@link Friend} is not found.
     *
     * @param friendId the id, as obtained using {@link Friend#getId()}.
     *
     * @return the {@link Friend}, never null
     */
    Friend getFriend(String friendId);

    /**
     * Deletes the supplied {@link Friend}, throwing an exception if the supplied id is not valid.
     *
     * @param friendId the id, as obtained using {@link Friend#getId()}.
     */
    void deleteFriend(String friendId);

}
