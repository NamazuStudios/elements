package dev.getelements.elements.service.auth;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserInfo;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.user.UserNotFoundException;
import dev.getelements.elements.model.user.User;
import org.dozer.Mapper;

import javax.inject.Inject;

import static dev.getelements.elements.model.user.User.Level.USER;

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
