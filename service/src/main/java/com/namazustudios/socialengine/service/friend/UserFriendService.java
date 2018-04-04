package com.namazustudios.socialengine.service.friend;

import com.namazustudios.socialengine.dao.FriendDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.friend.Friend;
import com.namazustudios.socialengine.service.FriendService;

import javax.inject.Inject;

public class UserFriendService implements FriendService {

    private User user;

    private FriendDao friendDao;

    @Override
    public Pagination<Friend> getFriends(final int offset, final int count) {
        return getFriendDao().getFriendsForUser(getUser(), offset, count);
    }

    @Override
    public Pagination<Friend> getFriends(final int offset, final int count, final String search) {
        return getFriendDao().getFriendsForUser(getUser(), offset, count, search);
    }

    @Override
    public Friend getFriend(final String friendId) {
        return getFriendDao().getFriendForUser(getUser(), friendId);
    }

    @Override
    public void deleteFriend(String friendId) {
        getFriendDao().deleteFriendForUser(getUser(), friendId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public FriendDao getFriendDao() {
        return friendDao;
    }

    @Inject
    public void setFriendDao(FriendDao friendDao) {
        this.friendDao = friendDao;
    }
    
}
