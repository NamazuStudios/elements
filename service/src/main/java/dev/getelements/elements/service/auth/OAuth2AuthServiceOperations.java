package dev.getelements.elements.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.auth.BodyType;
import dev.getelements.elements.sdk.model.auth.HttpMethod;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeValidationException;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.name.NameService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class OAuth2AuthServiceOperations {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthServiceOperations.class);

    private Client client;

    private NameService nameService;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private ApplicationDao applicationDao;

    private OAuth2AuthSchemeDao oAuth2AuthSchemeDao;

    private long sessionTimeoutSeconds;

    private ObjectMapper objectMapper;

    public SessionCreation createOrUpdateUserWithToken(
            final OAuth2SessionRequest oAuth2SessionRequest,
            final BiFunction<String, String, User> userMapper) {

        final var scheme = getOAuth2AuthSchemeDao().getAuthScheme(oAuth2SessionRequest.getSchemeId());
        final var resolved = resolve(scheme, oAuth2SessionRequest);
        final var parsed = execute(scheme, resolved);

        if (!isValidationSuccessful(parsed.status(), scheme, parsed.json())) {
            throw new AuthValidationException("Token validation failed.");
        }

        final var externalId = resolveExternalUserId(scheme, resolved, parsed.json());
        final var user = userMapper.apply(scheme.getName(), externalId);
        final var expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

        final var session = new Session();
        session.setUser(user);
        session.setExpiry(expiry);

        if (oAuth2SessionRequest.getProfileId() != null) {

            final var profile = profileDao.getActiveProfile(oAuth2SessionRequest.getProfileId());
            session.setProfile(profile);
            session.setApplication(profile.getApplication());

        } else if (oAuth2SessionRequest.getProfileSelector() != null) {

            final var profiles = profileDao.getActiveProfiles(0, 1, oAuth2SessionRequest.getProfileSelector());
            final var profile = profiles.stream().findFirst().orElse(null);

            if (profile != null) {
                session.setProfile(profile);
                session.setApplication(profile.getApplication());
            }
        }

        return getSessionDao().create(session);
    }

    private String resolveExternalUserId(final OAuth2AuthScheme scheme,
                                         final ResolvedRequest req,
                                         final JsonNode responseJson) {

        if (req.externalUserIdFromRequest() != null && !req.externalUserIdFromRequest().isBlank()) {
            return req.externalUserIdFromRequest();
        }

        final var mapping = scheme.getResponseIdMapping();
        if (mapping == null || mapping.isBlank()) {
            throw new InternalException("No external user id source configured. Set responseIdMapping or mark a request field with userId=true.");
        }

        final var node = findChildNodeByKey(responseJson, mapping);
        if (node.isNull()) {
            throw new InternalException("Response mapper mismatch! Could not find key: " + mapping);
        }
        return node.asText();
    }

    private ParsedResponse execute(final OAuth2AuthScheme scheme,
                                   final ResolvedRequest req) {

        var target = getClient().target(scheme.getValidationUrl());

        for (final var e : req.queryParams().entrySet()) {
            target = target.queryParam(e.getKey(), e.getValue());
        }

        final var builder = target.request(MediaType.APPLICATION_JSON_TYPE);

        for (final var e : req.headers().entrySet()) {
            builder.header(e.getKey(), e.getValue());
        }

        try (final Response response = invoke(builder, scheme, req)) {

            final var raw = response.readEntity(String.class);
            final var json = (raw == null || raw.isBlank()) ? getObjectMapper().nullNode() : getObjectMapper().readTree(raw);

            return new ParsedResponse(response.getStatus(), raw, json);

        } catch (Exception e) {
            throw new InternalException("OAuth2 validation request failed: " + e.getMessage(), e);
        }
    }

    private Response invoke(final Invocation.Builder builder,
                            final OAuth2AuthScheme scheme,
                            final ResolvedRequest req) {

        final var method = scheme.getMethod() == null ? HttpMethod.GET : scheme.getMethod();

        return switch (method) {
            case GET -> builder.get();
            case POST -> post(builder, scheme, req);
        };
    }

    private Response post(final Invocation.Builder builder,
                          final OAuth2AuthScheme scheme,
                          final ResolvedRequest req) {

        final var bodyType = scheme.getBodyType() == null ? BodyType.NONE : scheme.getBodyType();

        return switch (bodyType) {
            case FORM_URL_ENCODED -> {

                final var form = new Form();

                for (final var e : req.bodyParams().entrySet()) {
                    form.param(e.getKey(), e.getValue());
                }

                yield builder.post(Entity.form(form));
            }
            case JSON -> builder.post(Entity.entity(req.bodyParams(), MediaType.APPLICATION_JSON_TYPE));
            case NONE -> builder.post(null);
        };
    }

    private ResolvedRequest resolve(final OAuth2AuthScheme scheme,
                                    final OAuth2SessionRequest sessionRequest) {

        final var clientParams = sessionRequest.getRequestParameters() == null ?
                Map.<String, String>of() :
                sessionRequest.getRequestParameters();

        final var headers = resolveList(scheme.getHeaders(), clientParams);
        final var query = resolveList(scheme.getParams(), clientParams);
        final var body = resolveList(scheme.getBody(), clientParams);

        // find the one kv marked userId across headers/params/body
        final var userIdKv = Stream.of(scheme.getHeaders(), scheme.getParams(), scheme.getBody())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(OAuth2RequestKeyValue::isUserId)
                .findFirst()
                .orElse(null);

        if (userIdKv != null) {

            final var externalUserIdFromRequest = userIdKv.isFromClient()
                    ? clientParams.get(userIdKv.getKey())
                    : userIdKv.getValue();

            if (externalUserIdFromRequest == null || externalUserIdFromRequest.isBlank()) {
                throw new AuthSchemeValidationException("Auth scheme requires a user id, but it was not provided for key: " + userIdKv.getKey());
            }

            return new ResolvedRequest(headers, query, body, externalUserIdFromRequest);
        }

        return new ResolvedRequest(headers, query, body, null);
    }

    private Map<String, String> resolveList(
            final List<OAuth2RequestKeyValue> list,
            final Map<String, String> clientParams) {

        if (list == null || list.isEmpty()) {
            return Map.of();
        }

        final var out = new LinkedHashMap<String, String>();

        for (final var kv : list) {

            final var val = kv.isFromClient() ? clientParams.get(kv.getKey()) : kv.getValue();

            if (val != null && !val.isBlank()) {
                out.put(kv.getKey(), val);
            }
        }

        return Map.copyOf(out);
    }

    private boolean isValidationSuccessful(final int status,
                                           final OAuth2AuthScheme scheme,
                                           final JsonNode body) {

        if (scheme.getValidStatusCodes() == null || !scheme.getValidStatusCodes().contains(status)) {
            return false;
        }

        final var key = scheme.getResponseValidMapping();

        if (key == null || key.isBlank()) {
            return true; // status-only schemes
        }

        final var node = findChildNodeByKey(body, key);

        if (node.isNull()) {
            return false;
        }

        final var expected = scheme.getResponseValidExpectedValue();

        if (expected != null) {
            return expected.equals(node.asText());
        }

        if (node.isBoolean()) {
            return node.booleanValue();
        }

        return !node.asText().isBlank();
    }

    private JsonNode findChildNodeByKey(final JsonNode jsonNode, final String key) {

        final var node = jsonNode.findValue(key);

        if(node == null) {
            throw new InternalException("Response mapper mismatch!");
        }

        return node;
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

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private record ResolvedRequest(
            Map<String, String> headers,
            Map<String, String> queryParams,
            Map<String, String> bodyParams,
            String externalUserIdFromRequest // resolved value if any kv.userId == true
    ) {}

    private record ParsedResponse(
            int status,
            String rawBody,
            JsonNode json
    ) {}

}
