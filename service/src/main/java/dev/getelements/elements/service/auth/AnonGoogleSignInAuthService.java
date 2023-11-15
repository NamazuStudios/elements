package dev.getelements.elements.service.auth;

import dev.getelements.elements.dao.GoogleSignInUserDao;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.GoogleSignInAuthService;
import dev.getelements.elements.service.auth.GoogleSignInAuthServiceOperations.Claim;

import javax.inject.Inject;

import static dev.getelements.elements.model.user.User.Level.USER;

public class AnonGoogleSignInAuthService implements GoogleSignInAuthService {

    private GoogleSignInUserDao googleSignInUserDao;

    private GoogleSignInAuthServiceOperations googleSignInAuthServiceOperations;

    @Override
    public GoogleSignInSessionCreation createOrUpdateUserWithIdentityTokenAndAuthCode(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String identityToken,
            final String authorizationCode) {
        return getGoogleSignInAuthServiceOperations().createOrUpdateUserWithGoogleSignInTokenAndAuthorizationCode(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                identityToken,
                authorizationCode,
                googleIdentityToken ->  {
                    final User user = new User();
                    user.setActive(true);
                    user.setLevel(USER);
                    user.setName(googleIdentityToken.getClaim(Claim.USER_ID.value).asString());
                    user.setEmail(googleIdentityToken.getClaim(Claim.EMAIL.value).asString());
                    user.setGoogleSignInId(googleIdentityToken.getClaim(Claim.USER_ID.value).asString());
                    return getGoogleSignInUserDao().createReactivateOrUpdateUser(user);
                }
        );
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
