package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.CaptchaPublicConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyRequest;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyResponse;

/**
 * Standalone, general-purpose CAPTCHA verification service. Behavior does not vary by caller identity, so
 * (unlike most service interfaces) this is bound identically for every access level rather than switched by a
 * level-based {@link jakarta.inject.Provider}. Deliberately does not extend any other bound service interface,
 * to remain bridgeable across the Guice-HK2 boundary for injection into JAX-RS resources.
 */
@ElementPublic
@ElementServiceExport
public interface CaptchaService {

    /**
     * Returns the public, unauthenticated bootstrap information any frontend needs to decide whether to render
     * a CAPTCHA widget and, if so, which provider and site key to use.
     *
     * @return the {@link CaptchaPublicConfiguration}
     */
    CaptchaPublicConfiguration getPublicConfiguration();

    /**
     * Verifies a CAPTCHA response token against the configured provider, independent of any specific login
     * flow.
     *
     * @param request the request with the token to verify
     * @return the {@link CaptchaVerifyResponse}
     */
    CaptchaVerifyResponse verify(CaptchaVerifyRequest request);

}
