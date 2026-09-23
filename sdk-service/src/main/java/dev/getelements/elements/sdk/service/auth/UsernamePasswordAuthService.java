package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.session.MfaVerifyRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.util.security.BasicAuthorizationHeader;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Created by patricktwohig on 4/1/15.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface UsernamePasswordAuthService {

    /**
     * Performs a sign-in with the supplied {@link UsernamePasswordSessionRequest}.
     *
     * @param basicAuthorizationHeader the basic authorization header
     *
     * @return the {@link SessionCreation} if the session was created successfully.
     */
    default SessionCreation createSession(BasicAuthorizationHeader basicAuthorizationHeader) {
        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(basicAuthorizationHeader.getUsername());
        request.setPassword(basicAuthorizationHeader.getPassword());
        return createSession(request);
    }

    /**
     * Performs a sign-in with the supplied {@link UsernamePasswordSessionRequest}.
     *
     * @param usernamePasswordSessionRequest the user name password session request
     *
     * @return the {@link SessionCreation} if the session was created successfully.
     */
    SessionCreation createSession(UsernamePasswordSessionRequest usernamePasswordSessionRequest);

    /**
     * Completes a login that was interrupted by a TOTP challenge (see
     * {@link dev.getelements.elements.sdk.model.exception.auth.MfaChallengeRequiredException}), verifying the
     * submitted code and, on success, creating the session exactly as {@link #createSession} would have.
     *
     * @param mfaVerifyRequest the challenge ID and code
     * @return the {@link SessionCreation} if the code was valid
     */
    default SessionCreation completeMfaChallenge(MfaVerifyRequest mfaVerifyRequest) {
        throw new UnsupportedOperationException("MFA is not supported by this implementation.");
    }

}
