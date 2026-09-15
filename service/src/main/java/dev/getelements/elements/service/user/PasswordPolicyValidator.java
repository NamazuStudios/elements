package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.getelements.elements.sdk.model.Constants.PASSWORD_POLICY_DESCRIPTION;
import static dev.getelements.elements.sdk.model.Constants.PASSWORD_POLICY_REGEX;

/**
 * Enforces the system-wide, deploy-time-configured password policy against a raw, client-submitted
 * password. Not applied to server-generated passwords (e.g. mock/bootstrap accounts), which are not
 * subject to end-user policy requirements.
 */
@Singleton
public class PasswordPolicyValidator {

    private String policyRegex;

    private String policyDescription;

    /**
     * Validates the supplied raw password against the configured policy regex.
     *
     * @param rawPassword the raw, plain-text password to validate
     * @throws InvalidParameterException if the password does not match the configured policy, with a
     *                                   message including the configured policy description
     */
    public void validate(final String rawPassword) {

        final Pattern pattern;

        try {
            pattern = Pattern.compile(getPolicyRegex());
        } catch (final PatternSyntaxException ex) {
            // A misconfigured operator-supplied regex should not silently disable the policy nor
            // reject every password; surface the misconfiguration instead.
            throw new IllegalStateException(
                    "Configured password policy regex is invalid: " + getPolicyRegex(), ex);
        }

        if (rawPassword == null || !pattern.matcher(rawPassword).matches()) {
            throw new InvalidParameterException(getPolicyDescription());
        }

    }

    public String getPolicyRegex() {
        return policyRegex;
    }

    @Inject
    public void setPolicyRegex(@Named(PASSWORD_POLICY_REGEX) String policyRegex) {
        this.policyRegex = policyRegex;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }

    @Inject
    public void setPolicyDescription(@Named(PASSWORD_POLICY_DESCRIPTION) String policyDescription) {
        this.policyDescription = policyDescription;
    }

}
