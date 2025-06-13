package dev.getelements.elements.service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.name.NameService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class OAuth2AuthServiceOperations {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthServiceOperations.class);

    private static final String RSA_ALGO = "RSA";

    private Client client;

    private NameService nameService;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private ApplicationDao applicationDao;

    private OAuth2AuthSchemeDao oAuth2AuthSchemeDao;

    private long sessionTimeoutSeconds;


    public SessionCreation createOrUpdateUserWithToken(
            final OAuth2SessionRequest oAuth2SessionRequest,
            final BiFunction<String, String, User> userMapper) {

        final var scheme = getOAuth2AuthSchemeDao().getAuthScheme(oAuth2SessionRequest.getSchemeId());
        final var url = buildUrl(scheme, oAuth2SessionRequest);

        final var requestBuilder = getClient()
                .target(url)
                .request(MediaType.APPLICATION_JSON_TYPE);

        for(OAuth2RequestKeyValue kv : scheme.getHeaders()) {
            if(kv.isFromClient()) {
                final var val = oAuth2SessionRequest.getRequestParameters().get(kv.getKey());
                requestBuilder.header(kv.getKey(), val);
            } else {
                requestBuilder.header(kv.getKey(), kv.getValue());
            }
        }

        for(OAuth2RequestKeyValue kv : scheme.getParams()) {
            if(kv.isFromClient()) {
                final var val = oAuth2SessionRequest.getRequestParameters().get(kv.getKey());
                requestBuilder.property(kv.getKey(), val);
            } else {
                requestBuilder.property(kv.getKey(), kv.getValue());
            }
        }

        final var response = requestBuilder.get();
        final var id = parseId(response, scheme.getResponseIdMapping());

        // Maps the user, writing it to the database if needed.
        final var user = userMapper.apply(scheme.getName(), id);
        final var expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        final var session = new Session();

        session.setUser(user);
        session.setExpiry(expiry);

        if(oAuth2SessionRequest.getProfileId() != null) {
            final var profile = profileDao.getActiveProfile(oAuth2SessionRequest.getProfileId());
            session.setProfile(profile);
            session.setApplication(profile.getApplication());
        } else if(oAuth2SessionRequest.getProfileSelector() != null) {
            final var profiles = profileDao.getActiveProfiles(0, 1, oAuth2SessionRequest.getProfileSelector());
            final var profile = profiles.stream().findFirst().orElse(null);

            if(profile != null) {
                session.setProfile(profile);
                session.setApplication(profile.getApplication());
            }
        }

        return getSessionDao().create(session);
    }

    private String parseId(final Response response, final String idMapping) {
        try {
            final var objectMapper = new ObjectMapper();
            final var responseBody = response.readEntity(String.class);
            final var jsonNode = objectMapper.readTree(responseBody);

            final var id = jsonNode.findValue(idMapping).toString();

            if(id == null) {
                throw new InternalException("Response mapper mismatch!");
            }

            return id;
        } catch (Exception e) {
            throw new InternalException("There was a problem parsing id from the response. Please ensure that the ticket has not expired.");
        }
    }

    private Profile map(final User user,
                        final Application application) {

        final var profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(getNameService().generateQualifiedName());
        profile.setApplication(application);

        return profile;
    }

    private String buildUrl(final OAuth2AuthScheme scheme, final OAuth2SessionRequest request) {

        if(request.getRequestParameters() != null && !request.getRequestParameters().isEmpty()) {
            return scheme.getValidationUrl() + "?" + scheme.getParams().stream()
                            .map(r -> {

                                if(r.isFromClient()) {
                                    final var val = request.getRequestParameters().get(r.getKey());
                                    return val != null ? r.getKey() + "=" + val : "";
                                }

                                return r.getKey() + "=" + r.getValue();
                            })
                            .collect(Collectors.joining("&"));
        }

        return scheme.getValidationUrl();
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

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao SessionDao) {
        this.sessionDao = SessionDao;
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

    public OAuth2AuthSchemeDao getOAuth2AuthSchemeDao() {
        return oAuth2AuthSchemeDao;
    }

    @Inject
    public void setOAuth2AuthSchemeDao(OAuth2AuthSchemeDao oAuth2AuthSchemeDao) {
        this.oAuth2AuthSchemeDao = oAuth2AuthSchemeDao;
    }


}
