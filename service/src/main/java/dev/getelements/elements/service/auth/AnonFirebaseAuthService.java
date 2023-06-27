package dev.getelements.elements.service.auth;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserInfo;
import dev.getelements.elements.model.user.User;

import static dev.getelements.elements.model.user.User.Level.USER;

public class AnonFirebaseAuthService extends AbstractFirebaseAuthService {

    @Override
    protected User getUserFromUserInfo(final UserInfo userInfo) throws FirebaseAuthException {
        var user = new User();
        user.setLevel(USER);
        user.setActive(true);
        user.setName(userInfo.getUid());
        user.setEmail(userInfo.getEmail());
        user.setFirebaseId(userInfo.getUid());
        return getFirebaseUserDao().createReactivateOrUpdateUser(user);
    }

}
