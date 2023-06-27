package dev.getelements.elements.service.friend;

import dev.getelements.elements.dao.FriendDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.friend.Friend;
import dev.getelements.elements.service.FriendService;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class UserFriendService implements FriendService {

    private User user;

    private FriendDao friendDao;

    @Override
    public Pagination<Friend> getFriends(final int offset, final int count) {
        return getFriendDao().getFriendsForUser(getUser(), offset, count).transform(this::redactPrivateInformation);
    }

    @Override
    public Pagination<Friend> getFriends(final int offset, final int count, final String search) {
        return getFriendDao().getFriendsForUser(getUser(), offset, count, search).transform(this::redactPrivateInformation);
    }

    @Override
    public Friend getFriend(final String friendId) {
        return getFriendDao().getFriendForUser(getUser(), friendId);
    }

    @Override
    public void deleteFriend(final String friendId) {
        getFriendDao().deleteFriendForUser(getUser(), friendId);
    }

    private Friend redactPrivateInformation(final Friend friend) {

        friend.getUser().setEmail(null);
        friend.setProfiles(friend.getProfiles().stream()
            .map(p -> {p.getUser().setEmail(null); return p;})
            .collect(Collectors.toList())
        );

        return friend;
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
