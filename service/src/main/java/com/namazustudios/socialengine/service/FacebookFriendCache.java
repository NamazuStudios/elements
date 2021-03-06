package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.function.Supplier;

/**
 * Provides access to a cache for operations related to {@link FacebookFriend} instances.
 */
public interface FacebookFriendCache {

    /**
     * Given the user id, offset, count, and laoder function, this will load and return cached values for
     * {@link FacebookFriend}.
     *
     * @param userId the user id
     * @param offset the offset
     * @param count the count
     * @param loader the {@link Supplier<Iterable<FacebookFriend>>} which will load all values into the cache.
     * @return a {@link Pagination} with the results
     */
    Pagination<FacebookFriend> getUninvitedFriends(String userId,
                                                   int offset, int count,
                                                   Supplier<Iterable<FacebookFriend>> loader);

}
