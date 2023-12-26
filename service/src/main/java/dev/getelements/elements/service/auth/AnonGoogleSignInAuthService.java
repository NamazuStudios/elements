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

    // Per https://developers.google.com/identity/sign-in/web/backend-auth:
    // These six fields are included in all Google ID Tokens.
    // "iss": "https://accounts.google.com",
    // "sub": "110169484474386276334",
    // "azp": "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com",
    // "aud": "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com",
    // "iat": "1433978353",
    // "exp": "1433981953",
    //
    // These seven fields are only included when the user has granted the "profile" and "email" OAuth scopes to the application.
    // "email": "testuser@gmail.com",
    // "email_verified": "true",
    // "name" : "Test User",
    // "picture": "https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg",
    // "given_name": "Test",
    // "family_name": "User",
    // "locale": "en"
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
