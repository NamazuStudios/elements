package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.CaptchaConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaProvider;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateCaptchaConfigurationRequest;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.ValidationFailureException;
import dev.getelements.elements.service.auth.captcha.SuperUserCaptchaConfigurationService;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class SuperUserCaptchaConfigurationServiceTest {

    @Inject
    private SuperUserCaptchaConfigurationService service;

    @Inject
    private CaptchaConfigurationDao captchaConfigurationDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    public void testGetConfigurationDelegatesToDaoAndRedactsSecret() {

        final var stored = new CaptchaConfiguration();
        stored.setId("config-1");
        stored.setProvider(CaptchaProvider.RECAPTCHA);
        stored.setSiteKey("site-key");
        stored.setSecretKey("super-secret");
        stored.setEnabled(true);

        when(captchaConfigurationDao.getConfiguration()).thenReturn(stored);

        final var result = service.getConfiguration();

        assertEquals(result.getId(), "config-1");
        assertEquals(result.getSiteKey(), "site-key");
        assertTrue(result.isEnabled());
        assertNull(result.getSecretKey());

    }

    @Test
    public void testFirstTimeConfigurationRequiresSecretKey() {

        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.empty());

        final var request = validRequest();
        request.setSecretKey(null);

        assertThrows(InvalidParameterException.class, () -> service.updateConfiguration(request));
        verify(captchaConfigurationDao, never()).updateConfiguration(any());

    }

    @Test
    public void testCreatePropagatesFieldsAndRedactsSecret() {

        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.empty());
        when(captchaConfigurationDao.updateConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        final var response = service.updateConfiguration(validRequest());

        final var captor = ArgumentCaptor.forClass(CaptchaConfiguration.class);
        verify(captchaConfigurationDao).updateConfiguration(captor.capture());

        assertEquals(captor.getValue().getProvider(), CaptchaProvider.RECAPTCHA);
        assertEquals(captor.getValue().getSiteKey(), "site-key");
        assertEquals(captor.getValue().getSecretKey(), "super-secret");
        assertTrue(captor.getValue().isEnabled());

        assertNull(response.getSecretKey());

    }

    @Test
    public void testUpdateWithBlankSecretKeyPreservesExisting() {

        final var existing = new CaptchaConfiguration();
        existing.setId("config-1");
        existing.setSecretKey("original-secret");
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(existing));
        when(captchaConfigurationDao.updateConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        final var request = validRequest();
        request.setSecretKey(null);

        service.updateConfiguration(request);

        final var captor = ArgumentCaptor.forClass(CaptchaConfiguration.class);
        verify(captchaConfigurationDao).updateConfiguration(captor.capture());

        assertEquals(captor.getValue().getSecretKey(), "original-secret");

    }

    @Test
    public void testUpdateWithNewSecretKeyOverwritesExisting() {

        final var existing = new CaptchaConfiguration();
        existing.setId("config-1");
        existing.setSecretKey("original-secret");
        when(captchaConfigurationDao.findConfiguration()).thenReturn(Optional.of(existing));
        when(captchaConfigurationDao.updateConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        service.updateConfiguration(validRequest());

        final var captor = ArgumentCaptor.forClass(CaptchaConfiguration.class);
        verify(captchaConfigurationDao).updateConfiguration(captor.capture());

        assertEquals(captor.getValue().getSecretKey(), "super-secret");

    }

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testMissingSiteKeyFailsValidation() {
        final var request = validRequest();
        request.setSiteKey(null);
        service.updateConfiguration(request);
    }

    private static CaptchaConfiguration copyOf(final CaptchaConfiguration source) {
        final var copy = new CaptchaConfiguration();
        copy.setId(source.getId());
        copy.setProvider(source.getProvider());
        copy.setSiteKey(source.getSiteKey());
        copy.setSecretKey(source.getSecretKey());
        copy.setEnabled(source.isEnabled());
        return copy;
    }

    private static CreateOrUpdateCaptchaConfigurationRequest validRequest() {
        final var request = new CreateOrUpdateCaptchaConfigurationRequest();
        request.setProvider(CaptchaProvider.RECAPTCHA);
        request.setSiteKey("site-key");
        request.setSecretKey("super-secret");
        request.setEnabled(true);
        return request;
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(CaptchaConfigurationDao.class).toInstance(mock(CaptchaConfigurationDao.class));
            bind(jakarta.validation.Validator.class).toInstance(buildDefaultValidatorFactory().getValidator());
            bind(SuperUserCaptchaConfigurationService.class);
        }

    }

}
