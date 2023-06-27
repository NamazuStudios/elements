package dev.getelements.elements.dao;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.friend.Friend;

import java.util.List;

/**
 * Manages {@link Friend} instances with {@link User} instance withint he context of Facebook.
 */
public interface FacebookFriendDao {

    /**
     * Provided a {@link User}, this will create a {@link Friend} association for Facebook user.  The friends will be
     * presumed to be mutual.
     *
     * @param user the {@link User}
     * @param facebookIds the {@link List <String>}  containing the facebook ID of each {@link User}.
     *
     */
    void associateFriends(User user, List<String> facebookIds);

}
