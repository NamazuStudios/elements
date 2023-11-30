package dev.getelements.elements.service.auth;

import com.auth0.jwt.impl.PublicClaims;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.gson.GsonFactory;
import dev.getelements.elements.dao.GoogleSignInApplicationConfigurationDao;
import dev.getelements.elements.dao.GoogleSignInSessionDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.application.ApplicationConfigurationNotFoundException;
import dev.getelements.elements.model.application.GoogleSignInApplicationConfiguration;
import dev.getelements.elements.model.googlesignin.TokenResponse;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;

public class GoogleSignInAuthServiceOperations {

    private Client client;

    private NameService nameService;

    private ProfileDao profileDao;

    private GoogleSignInSessionDao googleSignInSessionDao;

    private GoogleSignInApplicationConfigurationDao googleSignInApplicationConfigurationDao;

    public GoogleSignInSessionCreation createOrUpdateUserWithGoogleSignInToken(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String identityToken,
            final Function<GoogleIdToken, User> userMapper) {

        try {

            final var appConfiguration = getApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId
            );

            //Verify that the audience matches the registered Google Application ID
            final var verifier = new GoogleIdTokenVerifier
                    .Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(appConfiguration.getClientIds().values())
                    .build();

            // Attempts to validate the identity, and if it is not valid presumes that the request should be forbidden.
            final var idToken = verifier.verify(identityToken);

            if (idToken != null) {

                // Maps the user, writing it to the database.
                final var user = userMapper.apply(idToken);
                final var imageUrl = (String) idToken.getPayload().get("picture");
                final var profile = getProfileDao().createOrRefreshProfile(map(
                        user,
                        appConfiguration,
                        imageUrl)
                );

                final var session = new Session();
                session.setUser(user);
                session.setProfile(profile);
                session.setApplication(profile.getApplication());

                final var tokenResponse = new TokenResponse();
                tokenResponse.setAccessToken(idToken.getPayload().getAccessTokenHash());
                tokenResponse.setIdToken(identityToken);
                tokenResponse.setExpiresAt(idToken.getPayload().getExpirationTimeSeconds());
                return getGoogleSignInSessionDao().create(session, tokenResponse);
            }

            throw new ForbiddenException("Cannot verify ID token. Please ensure that your Application is configured properly.");

        } catch (GeneralSecurityException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    private Profile map(final User user,
                        final GoogleSignInApplicationConfiguration googleSignInApplicationConfiguration,
                        final String imageUrl) {
        final var profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(getNameService().generateQualifiedName());
        profile.setApplication(googleSignInApplicationConfiguration.getParent());

        final var profileImage = new LargeObjectReference();
        profileImage.setUrl(imageUrl);
        profile.setImageObject(profileImage);

        return profile;
    }

    private GoogleSignInApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                    final String applicationConfigurationNameOrId) {

        final GoogleSignInApplicationConfiguration appConfiguration;

        try {
            appConfiguration = getGoogleApplicationConfigurationDao().getApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId);
        } catch (ApplicationConfigurationNotFoundException ex) {
            throw new InternalException(ex);
        }

        return appConfiguration;

    }


    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public GoogleSignInSessionDao getGoogleSignInSessionDao() {
        return googleSignInSessionDao;
    }

    @Inject
    public void setGoogleSignInSessionDao(GoogleSignInSessionDao googleSignInSessionDao) {
        this.googleSignInSessionDao = googleSignInSessionDao;
    }

    public GoogleSignInApplicationConfigurationDao getGoogleApplicationConfigurationDao() {
        return googleSignInApplicationConfigurationDao;
    }

    @Inject
    public void setGoogleSignInApplicationConfigurationDao(GoogleSignInApplicationConfigurationDao googleSignInApplicationConfigurationDao) {
        this.googleSignInApplicationConfigurationDao = googleSignInApplicationConfigurationDao;
    }

}
