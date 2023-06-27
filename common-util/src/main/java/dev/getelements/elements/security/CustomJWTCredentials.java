package dev.getelements.elements.security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.exception.security.AuthorizationHeaderParseException;
import dev.getelements.elements.model.auth.PrivateClaim;
import dev.getelements.elements.model.auth.UserKey;
import dev.getelements.elements.model.user.User;

import java.util.Map;

/**
 * JWT Credentials used with custom auth schemes.
 */
public class CustomJWTCredentials {

    public static final String AUTH_TYPE = "custom";

    private final String subject;

    private final UserKey userKey;

    private final Map<String, Object> user;

    private final JWTCredentials credentials;

    /**
     * Creates a {@link CustomJWTCredentials} from the supplied {@link DecodedJWT}. Throwing the appropriate exception
     * if the JWT cannot be understood.
     *
     * @param credential                                                                                                                                                                                                                                                                        s the decoded JWT
     */
    public CustomJWTCredentials(final JWTCredentials credentials) {

        this.credentials = credentials;

        final var type = credentials.getDecoded().getClaim(PrivateClaim.AUTH_TYPE.getValue()).asString();

        if (!AUTH_TYPE.equals(type))
            throw new AuthorizationHeaderParseException("Invalid Auth Type.");

        subject = credentials.getDecoded().getSubject();

        if (subject == null)
            throw new AuthorizationHeaderParseException("Invalid subject.");

        final var userKeyString = credentials.getDecoded().getClaim(PrivateClaim.USER_KEY.getValue()).asString();

        userKey = UserKey
            .findByValue(userKeyString)
            .orElseThrow(() -> new AuthorizationHeaderParseException("Invalid User Key: " + userKeyString));

        try {
            user = credentials.getDecoded().getClaim(PrivateClaim.USER.getValue()).asMap();
        } catch (JWTDecodeException ex) {
            throw new AuthorizationHeaderParseException(ex);
        }

    }

    /**
     * Gets the subject of the
     *
     * @return gets the subject.
     *
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the {@link UserKey}.
     *
     * @return the user key
     */
    public UserKey getUserKey() {
        return userKey;
    }

    /**
     * Gets the raw user as {@link Map}
     *
     * @return the raw user object.
     */
    public Map<String, Object> getUser() {
        return user;
    }

    /**
     * Gets the {@link JWTCredentials} that was used to make this instance.
     *
     * @return the {@link JWTCredentials}
     */
    public JWTCredentials getCredentials() {
        return credentials;
    }

}
