package dev.getelements.elements.service;

import dev.getelements.elements.sdk.dao.CaptchaConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaProvider;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.service.auth.captcha.DefaultCaptchaService;
import dev.getelements.elements.service.auth.captcha.RecaptchaSiteVerifyResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class DefaultCaptchaServiceTest {

    private CaptchaConfigurationDao captchaConfigurationDao;

    private Client client;

    private WebTarget target;

    private Invocation.Builder requestBuilder;

    private Response response;

    private DefaultCaptchaService service;

    @BeforeMethod
    public void setup() {

        captchaConfigurationDao = mock(CaptchaConfigurationDao.class);

        client = mock(Client.class);
        target = mock(WebTarget.class);
        requestBuilder = mock(Invocation.Builder.class);
        response = mock(Response.class);

        when(client.target(anyString())).thenReturn(target);
        when(target.request(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.post(any())).thenReturn(response);

        final var validationHelper = new ValidationHelper();
        try {
            final var field = ValidationHelper.class.getDeclaredField("validator");
            field.setAccessible(true);
            field.set(validationHelper, buildDefaultValidatorFactory().getValidator());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        service = new DefaultCaptchaService();
        service.setCaptchaConfigurationDao(captchaConfigurationDao);
        service.setValidationHelper(validationHelper);
        service.setClient(client);

    }

    @Test
    public void testPublicConfigurationHiddenWhenDisabled() {

        final var config = new CaptchaConfiguration();
        config.setEnabled(false);
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(config));

        final var result = service.getPublicConfiguration();

        assertFalse(result.isEnabled());
        assertNull(result.getSiteKey());

    }

    @Test
    public void testPublicConfigurationHiddenWhenNeverConfigured() {
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.empty());
        assertFalse(service.getPublicConfiguration().isEnabled());
    }

    @Test
    public void testPublicConfigurationExposesSiteKeyWhenEnabled() {

        final var config = new CaptchaConfiguration();
        config.setEnabled(true);
        config.setProvider(CaptchaProvider.RECAPTCHA);
        config.setSiteKey("site-key");
        config.setSecretKey("super-secret");
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(config));

        final var result = service.getPublicConfiguration();

        assertTrue(result.isEnabled());
        assertEquals(result.getProvider(), CaptchaProvider.RECAPTCHA);
        assertEquals(result.getSiteKey(), "site-key");

    }

    @Test
    public void testVerifyFailsWhenNotConfigured() {

        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.empty());

        final var request = new CaptchaVerifyRequest();
        request.setToken("some-token");

        assertFalse(service.verify(request).isSuccess());
        verifyNoInteractions(client);

    }

    @Test
    public void testVerifyFailsWhenDisabled() {

        final var config = new CaptchaConfiguration();
        config.setEnabled(false);
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(config));

        final var request = new CaptchaVerifyRequest();
        request.setToken("some-token");

        assertFalse(service.verify(request).isSuccess());
        verifyNoInteractions(client);

    }

    @Test
    public void testVerifySucceedsWhenProviderConfirms() {

        final var config = enabledConfig();
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(config));

        final var siteVerifyResponse = new RecaptchaSiteVerifyResponse();
        siteVerifyResponse.setSuccess(true);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(RecaptchaSiteVerifyResponse.class)).thenReturn(siteVerifyResponse);

        final var request = new CaptchaVerifyRequest();
        request.setToken("good-token");

        assertTrue(service.verify(request).isSuccess());
        verify(client).target("https://www.google.com/recaptcha/api/siteverify");
        verify(response).close();

    }

    @Test
    public void testVerifyFailsWhenProviderRejects() {

        final var config = enabledConfig();
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(config));

        final var siteVerifyResponse = new RecaptchaSiteVerifyResponse();
        siteVerifyResponse.setSuccess(false);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(RecaptchaSiteVerifyResponse.class)).thenReturn(siteVerifyResponse);

        final var request = new CaptchaVerifyRequest();
        request.setToken("bad-token");

        assertFalse(service.verify(request).isSuccess());

    }

    @Test
    public void testVerifyFailsWhenProviderReturnsNon200() {

        final var config = enabledConfig();
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(config));
        when(response.getStatus()).thenReturn(500);

        final var request = new CaptchaVerifyRequest();
        request.setToken("some-token");

        assertFalse(service.verify(request).isSuccess());
        verify(response).close();

    }

    private static CaptchaConfiguration enabledConfig() {
        final var config = new CaptchaConfiguration();
        config.setEnabled(true);
        config.setProvider(CaptchaProvider.RECAPTCHA);
        config.setSiteKey("site-key");
        config.setSecretKey("super-secret");
        return config;
    }

}
