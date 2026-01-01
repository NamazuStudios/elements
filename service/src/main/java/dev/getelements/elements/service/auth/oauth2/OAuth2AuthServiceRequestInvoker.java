package dev.getelements.elements.service.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.auth.BodyType;
import dev.getelements.elements.sdk.model.auth.HttpMethod;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.exception.InternalException;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class OAuth2AuthServiceRequestInvoker {

    private Client client;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private ObjectMapper objectMapper;

    public ParsedResponse execute(final OAuth2AuthScheme scheme,
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

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
