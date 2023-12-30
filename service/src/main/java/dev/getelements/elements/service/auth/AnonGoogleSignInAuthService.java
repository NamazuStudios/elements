package dev.getelements.elements.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.getelements.elements.dao.GoogleSignInUserDao;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.GoogleSignInAuthService;

import javax.inject.Inject;

import static dev.getelements.elements.model.user.User.Level.USER;

public class AnonGoogleSignInAuthService implements GoogleSignInAuthService {

    private GoogleSignInUserDao googleSignInUserDao;

    private GoogleSignInAuthServiceOperations googleSignInAuthServiceOperations;

    @Override
    public GoogleSignInSessionCreation createOrUpdateUserWithIdentityToken(
            final String applicationNameOrId,
            final String identityToken) {

        return getGoogleSignInAuthServiceOperations().createOrUpdateUserWithGoogleSignInToken(
                applicationNameOrId,
                identityToken,
                googleIdentityToken ->  {
                    final var user = mapTokenToUser(googleIdentityToken);
                    return getGoogleSignInUserDao().createReactivateOrUpdateUser(user);
                }
        );
    }

    private User mapTokenToUser(final GoogleIdToken googleIdentityToken) {

        final var payload = googleIdentityToken.getPayload();
        final var userId = payload.getSubject();
        final var email = payload.getEmail();

        final User user = new User();
        user.setActive(true);
        user.setLevel(USER);
        user.setName(email);
        user.setEmail(email);
        user.setGoogleSignInId(userId);

        return user;
    }

    public GoogleSignInUserDao getGoogleSignInUserDao() {
        return googleSignInUserDao;
    }

    @Inject
    public void setGoogleSignInUserDao(GoogleSignInUserDao googleSignInUserDao) {
        this.googleSignInUserDao = googleSignInUserDao;
    }

    public GoogleSignInAuthServiceOperations getGoogleSignInAuthServiceOperations() {
        return googleSignInAuthServiceOperations;
    }

    @Inject
    public void setGoogleSignInAuthServiceOperations(GoogleSignInAuthServiceOperations googleSignInAuthServiceOperations) {
        this.googleSignInAuthServiceOperations = googleSignInAuthServiceOperations;
    }

}
