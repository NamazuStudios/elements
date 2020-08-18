package com.namazustudios.socialengine.service.auth;

import com.auritylab.kotlin.apple.signin.AppleIdentityToken;
import com.auritylab.kotlin.apple.signin.AppleSignIn;
import com.namazustudios.socialengine.dao.IosApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.application.ApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import java.util.function.Function;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static java.lang.String.format;

public class AppleSignInAuthServiceOperations {

    private IosApplicationConfigurationDao iosApplicationConfigurationDao;

    public AppleSignInSessionCreation createOrUpdateUserWithAppleSignInTokenAndAuthorizationCode(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String identityToken,
            final String authorizationCode,
            final Function<AppleIdentityToken, User> userMapper) {

        final AppleSignIn appleSignIn = getAppleSignIn(applicationNameOrId, applicationConfigurationNameOrId);
        final AppleIdentityToken appleIdentityToken = appleSignIn.validate(identityToken);
        if (appleIdentityToken == null) throw new ForbiddenException("Apple auth failed.");

        final User user = new User();
        final String email = appleIdentityToken.getEmail();

        user.setLevel(USER);
        user.setEmail(email);
        user.setAppleSignInId(appleIdentityToken.getUserIdentifier());

        final int atIndex = email.indexOf('@');
        final String userName =  email.substring(0, atIndex);
        user.setName(userName);

        return null; // TODO Implement this

    }

    private AppleSignIn getAppleSignIn(final String applicationNameOrId,
                                       final String applicationConfigurationNameOrId) {
        final String privateKey = getPrivateKey(applicationNameOrId, applicationConfigurationNameOrId);
        return new AppleSignIn(privateKey);
    }

    private String getPrivateKey(final String applicationNameOrId, final String applicationConfigurationNameOrId) {

        final IosApplicationConfiguration iosApplicationConfiguration;

        try {
            iosApplicationConfiguration = getIosApplicationConfigurationDao()
                    .getIosApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
        } catch (ApplicationConfigurationNotFoundException ex) {
            throw new InternalException(ex);
        }

        final String privateKey = iosApplicationConfiguration.getAppleSignInPrivateKey();

        if (privateKey == null) {
            final String msg = format("No private key configured for %s", iosApplicationConfiguration.getApplicationId());
            throw new InternalException(msg);
        }

        return privateKey;

    }

    public IosApplicationConfigurationDao getIosApplicationConfigurationDao() {
        return iosApplicationConfigurationDao;
    }

    @Inject
    public void setIosApplicationConfigurationDao(IosApplicationConfigurationDao iosApplicationConfigurationDao) {
        this.iosApplicationConfigurationDao = iosApplicationConfigurationDao;
    }

}
