package dev.getelements.elements.service.auth;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.GoogleSignInSessionDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.NameService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Function;

import static dev.getelements.elements.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class GoogleSignInAuthServiceOperations {

    private Client client;

    private NameService nameService;

    private ProfileDao profileDao;

    private GoogleSignInSessionDao googleSignInSessionDao;

    private ApplicationDao applicationDao;

    private long sessionTimeoutSeconds;

    public GoogleSignInSessionCreation createOrUpdateUserWithGoogleSignInToken(
            final String applicationNameOrId,
            final String identityToken,
            final Function<GoogleIdToken, User> userMapper) {

        try {

            final var decodedToken = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), identityToken);

            final var verified =
                    new GoogleIdTokenVerifier(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                    .verify(decodedToken);

            if(!verified) {
                throw new ForbiddenException("Could not verify ID token.");
            }

            final var user = userMapper.apply(decodedToken);
            final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
            final var session = new Session();

            session.setUser(user);
            session.setExpiry(expiry);

            if(applicationNameOrId != null) {

                final var application = getApplicationDao().getActiveApplication(applicationNameOrId);
                final var imageUrl = (String) decodedToken.getPayload().get("picture");
                final var profile = getProfileDao().createOrRefreshProfile(
                        map(user, application, imageUrl)
                );

                session.setProfile(profile);
                session.setApplication(application);
            }

            return getGoogleSignInSessionDao().create(session);

        } catch (GeneralSecurityException e) {
            throw new InternalException(e);
        } catch (TokenResponseException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    private Profile map(final User user,
                        final Application application,
                        final String imageUrl) {
        final var profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(getNameService().generateQualifiedName());
        profile.setApplication(application);

        final var profileImage = new LargeObjectReference();
        profileImage.setUrl(imageUrl);
        profile.setImageObject(profileImage);

        return profile;
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

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
