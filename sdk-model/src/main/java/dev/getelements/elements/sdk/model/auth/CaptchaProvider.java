package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** The third-party CAPTCHA provider backing a {@link CaptchaConfiguration}. */
@Schema(description = "The third-party CAPTCHA provider backing the system-wide CAPTCHA configuration.")
public enum CaptchaProvider {

    /** Google reCAPTCHA (v2 checkbox), verified against the {@code siteverify} endpoint. */
    RECAPTCHA

}
