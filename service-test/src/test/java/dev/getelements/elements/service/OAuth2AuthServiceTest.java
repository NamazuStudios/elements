package dev.getelements.elements.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.auth.BodyType;
import dev.getelements.elements.sdk.model.auth.HttpMethod;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.auth.oauth2.OAuth2AuthServiceOperations;
import dev.getelements.elements.service.auth.oauth2.OAuth2AuthServiceRequestInvoker;
import dev.getelements.elements.service.auth.oauth2.ParsedResponse;
import dev.getelements.elements.service.auth.oauth2.ResolvedRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class OAuth2AuthServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    private OAuth2AuthServiceOperations ops;

    @Inject
    private OAuth2AuthSchemeDao schemeDao;

    @Inject
    private OAuth2AuthServiceRequestInvoker invoker;

    @Inject
    private SessionDao sessionDao;

    @BeforeMethod
    public void setup() {

        final var injector = createInjector(new TestModule());
        injector.injectMembers(this);

        when(sessionDao.create(any(Session.class))).thenReturn(new SessionCreation());
    }

    private record Case(String name, OAuth2AuthScheme scheme, OAuth2SessionRequest req, int status, String responseBody,
                boolean expectSuccess, String expectedExternalUserId, Map<String, String> expectedQuery,
                Map<String, String> expectedBody) {

        @Override
        public String toString() {
            return name;
        }
    }

    @DataProvider(name = "cases")
    public Object[][] cases() {
        return new Object[][]{
                { case_get_responseIdMapping_success() },
                { case_get_isValid_false_fails() },
                { case_post_form_userId_from_request_success() },
                { case_post_form_status_not_allowed_fails() },
                { case_missing_client_userId_fails_fast() },
                { case_responseIdMapping_missing_key_fails() },
        };
    }

    @Test(dataProvider = "cases")
    public void testCreateOrUpdateUserWithToken(final Case testCase) {

        when(schemeDao.getAuthScheme(testCase.req.getSchemeId())).thenReturn(testCase.scheme);

        // invoker returns ParsedResponse with parsed JSON
        when(invoker.execute(eq(testCase.scheme), any(ResolvedRequest.class)))
                .thenAnswer(inv -> {
                    JsonNode json = testCase.responseBody == null || testCase.responseBody.isBlank()
                            ? MAPPER.nullNode()
                            : MAPPER.readTree(testCase.responseBody);
                    return new ParsedResponse(testCase.status, testCase.responseBody, json);
                });

        @SuppressWarnings("unchecked")
        final BiFunction<String, String, User> userMapper = mock(BiFunction.class);
        when(userMapper.apply(anyString(), anyString())).thenReturn(new User());

        if (testCase.expectSuccess) {
            final var out = ops.createOrUpdateUserWithToken(testCase.req, userMapper);
            assertNotNull(out);

            // verify userMapper got correct external id
            verify(userMapper).apply(eq(testCase.scheme.getName()), eq(testCase.expectedExternalUserId));

            // verify session created
            verify(sessionDao).create(any(Session.class));

            // verify resolved request mapping into query/body passed to invoker
            final var rrCap = ArgumentCaptor.forClass(ResolvedRequest.class);
            verify(invoker).execute(eq(testCase.scheme), rrCap.capture());

            final var rr = rrCap.getValue();

            assertEquals(rr.queryParams(), testCase.expectedQuery);
            assertEquals(rr.bodyParams(), testCase.expectedBody);

        } else {
            assertThrows(AuthValidationException.class, () ->
                    ops.createOrUpdateUserWithToken(testCase.req, userMapper));

            // should not create session on failure
            verify(sessionDao, never()).create(any(Session.class));
        }
    }

    // ---------- Case builders ----------

    private Case case_get_responseIdMapping_success() {

        final var scheme = baseScheme("steam_game1");
        scheme.setMethod(HttpMethod.GET);
        scheme.setBodyType(BodyType.NONE);
        scheme.setParams(List.of(
                new OAuth2RequestKeyValue("access_token", null, true, false)
        ));
        scheme.setResponseIdMapping("steamid");
        scheme.setResponseValidMapping(null); // status-only
        scheme.setValidStatusCodes(List.of(200));

        final var req = baseReq("scheme-1",
                Map.of("access_token", "abc"),
                Map.of());

        final var body = "{\"steamid\":\"76561198000000000\"}";

        return new Case(
                "GET + responseIdMapping success",
                scheme, req,
                200, body,
                true,
                "76561198000000000",
                Map.of("access_token", "abc"),
                Map.of()
        );
    }

    private Case case_get_isValid_false_fails() {

        final var scheme = baseScheme("meta_nonce");
        scheme.setMethod(HttpMethod.GET);
        scheme.setParams(List.of(new OAuth2RequestKeyValue("nonce", null, true, false)));
        scheme.setResponseValidMapping("is_valid"); // check body field
        scheme.setResponseValidExpectedValue("true");
        scheme.setValidStatusCodes(List.of(200));
        scheme.setResponseIdMapping("id"); // irrelevant, should fail before

        final var req = baseReq("scheme-2",
                Map.of("nonce", "n1"),
                Map.of());

        final var body = "{\"is_valid\":false,\"id\":\"u1\"}";

        return new Case(
                "GET + is_valid=false fails",
                scheme, req,
                200, body,
                false,
                null,
                Map.of("nonce", "n1"),
                Map.of()
        );
    }

    private Case case_post_form_userId_from_request_success() {
        OAuth2AuthScheme scheme = baseScheme("meta_nonce");
        scheme.setMethod(HttpMethod.POST);
        scheme.setBodyType(BodyType.FORM_URL_ENCODED);
        scheme.setBody(List.of(
                new OAuth2RequestKeyValue("access_token", "OC|app|secret", false, false),
                new OAuth2RequestKeyValue("nonce", null, true, false),
                new OAuth2RequestKeyValue("user_id", null, true, true) // external user id comes from request
        ));
        scheme.setResponseValidMapping("is_valid");
        scheme.setResponseValidExpectedValue("true");
        scheme.setValidStatusCodes(List.of(200));
        scheme.setResponseIdMapping(null); // not needed

        OAuth2SessionRequest req = baseReq("scheme-3",
                Map.of("nonce", "N", "user_id", "U"),
                Map.of());

        String body = "{\"is_valid\":true}";

        return new Case(
                "POST FORM + userId from request success",
                scheme, req,
                200, body,
                true,
                "U",
                Map.of(),
                Map.of("access_token", "OC|app|secret", "nonce", "N", "user_id", "U")
        );
    }

    private Case case_post_form_status_not_allowed_fails() {

        final var scheme = baseScheme("meta_nonce");
        scheme.setMethod(HttpMethod.POST);
        scheme.setBodyType(BodyType.FORM_URL_ENCODED);
        scheme.setBody(List.of(new OAuth2RequestKeyValue("user_id", null, true, true)));
        scheme.setValidStatusCodes(List.of(200)); // only 200 is allowed
        scheme.setResponseValidMapping("is_valid");
        scheme.setResponseValidExpectedValue("true");

        final var req = baseReq("scheme-4",
                Map.of("user_id", "U"),
                Map.of());

        final var body = "{\"is_valid\":true}";

        return new Case(
                "POST FORM + status not allowed fails",
                scheme, req,
                401, body,
                false,
                null,
                Map.of(),
                Map.of("user_id", "U")
        );
    }

    private Case case_missing_client_userId_fails_fast() {

        final var scheme = baseScheme("meta_nonce");
        scheme.setMethod(HttpMethod.POST);
        scheme.setBodyType(BodyType.FORM_URL_ENCODED);
        scheme.setBody(List.of(new OAuth2RequestKeyValue("user_id", null, true, true))); // required client value

        final var req = baseReq("scheme-5",
                Map.of(), // missing user_id
                Map.of());

        return new Case(
                "Missing client user_id fails before invoker",
                scheme, req,
                200, "{\"is_valid\":true}",
                false,
                null,
                Map.of(),
                Map.of()
        );
    }

    private Case case_responseIdMapping_missing_key_fails() {

        final var scheme = baseScheme("steam_game1");
        scheme.setMethod(HttpMethod.GET);
        scheme.setResponseIdMapping("steamid"); // expects steamid
        scheme.setValidStatusCodes(List.of(200));

        final var req = baseReq("scheme-6",
                Map.of(),
                Map.of());

        final var body = "{\"different\":\"value\"}"; // missing steamid

        return new Case(
                "responseIdMapping missing key fails",
                scheme, req,
                200, body,
                false,
                null,
                Map.of(),
                Map.of()
        );
    }

    // ---------- Helpers ----------

    private OAuth2AuthScheme baseScheme(String name) {

        final var s = new OAuth2AuthScheme();
        s.setId("ignored");
        s.setName(name);
        s.setValidationUrl("https://example.com/validate");
        s.setHeaders(List.of());
        s.setParams(List.of());
        s.setBody(List.of());
        // defaults: method GET, bodyType NONE, validStatusCodes [200]
        return s;
    }

    private OAuth2SessionRequest baseReq(String schemeId,
                                         Map<String, String> params,
                                         Map<String, String> headers) {

        final var r = new OAuth2SessionRequest();
        r.setSchemeId(schemeId);
        r.setRequestParameters(params);
        r.setRequestHeaders(headers);
        return r;
    }


    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(OAuth2AuthSchemeDao.class).toInstance(mock(OAuth2AuthSchemeDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(NameService.class).toInstance(mock(NameService.class));
            bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
            bind(Client.class).toInstance(mock(Client.class));
            bind(OAuth2AuthServiceRequestInvoker.class).toInstance(mock(OAuth2AuthServiceRequestInvoker.class));
            bindConstant().annotatedWith(Names.named(SESSION_TIMEOUT_SECONDS)).to(3600L);
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }
    }

}
