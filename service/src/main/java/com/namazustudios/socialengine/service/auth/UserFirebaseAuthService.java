package com.namazustudios.socialengine.service.auth;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserInfo;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.user.User;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.Level.USER;

public class UserFirebaseAuthService extends AbstractFirebaseAuthService {

    private User user;

    private Mapper mapper;

    @Override
    protected User getUserFromUserInfo(final UserInfo userInfo) throws FirebaseAuthException {

        final var user = getMapper().map(getUser(), User.class);

        user.setName(userInfo.getUid());
        user.setEmail(userInfo.getEmail());
        user.setFirebaseId(userInfo.getUid());

        try {
            return getFirebaseUserDao().connectActiveUserIfNecessary(user);
        } catch (UserNotFoundException ex) {
            throw new ForbiddenException(ex);
        }

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

}
