package com.namazustudios.socialengine.security;

import com.google.common.base.Splitter;
import com.namazustudios.socialengine.Headers;
import com.namazustudios.socialengine.exception.security.BadSessionSecretException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.SessionCreation;

import java.util.Iterator;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static java.util.regex.Pattern.compile;

/**
 * Parses the value in the session secret header specified in {@link Headers#SESSION_SECRET}.
 */
public class SessionSecretHeader {

    private static final Pattern SEPARATOR = compile("\\s+");

    private static final Pattern SECRET_PATTERN = compile("\\w+");

    private static final Pattern OVERRIDE_USER_ID_PATTERN = compile("u\\w+");

    private static final Pattern OVERRIDE_PROFILE_ID_PATTERN = compile("p\\w+");

    private final String sessionSecret;

    private final String overrideUserId;

    private final String overrideProfileId;

    /**
     * Given the supplied {@link Function<String, String>}, this will extract the session secret value and return parse
     * it using the alternte constructor {@link #SessionSecretHeader(String)}.  The supplied function will be passed
     * with the argument {@link Headers#SESSION_SECRET} and must either return the value of the session secret or it
     * must return null.  If value returned does not fit the format of the session secret header, then this will raise
     * and instance of {@Link BadSessionSecretException}.
     *
     * @param headerSupplierFunction, may return null
     */
    public SessionSecretHeader(final Function<String, String> headerSupplierFunction) {
        this(getSessionSecretHeader(headerSupplierFunction));
    }

    @SuppressWarnings("deprecated") // This is here to provide backwards compatibility.
    private static String getSessionSecretHeader(final Function<String, String> headerSupplierFunction) {
        final String secret = headerSupplierFunction.apply(SESSION_SECRET);
        return secret == null ? headerSupplierFunction.apply(SOCIALENGINE_SESSION_SECRET) : secret;
    }

    /**
     * Parses out the session secret header and validates all tokens therein.  This will raise an instance of
     * {@link BadSessionSecretException} in the event of parse failure.
     *
     * @param header the header value, may be null
     */
    public SessionSecretHeader(final String header) {

        if (header == null) {
            sessionSecret = null;
            overrideUserId = null;
            overrideProfileId = null;
            return;
        }

        final Iterator<String> tokens = Splitter.on(SEPARATOR)
                .trimResults()
                .omitEmptyStrings()
                .split(header)
                .iterator();

        if (!tokens.hasNext()) bail();

        sessionSecret = tokens.next();
        if (!SECRET_PATTERN.matcher(sessionSecret).find()) bail();

        String overrideUserId = null;
        String overrideProfileId = null;

        while (tokens.hasNext()) {

            boolean matches = false;

            final String token = tokens.next();

            if (OVERRIDE_USER_ID_PATTERN.matcher(token).find()) {
                matches = true;
                overrideUserId = token.substring(1);
            } else if (OVERRIDE_PROFILE_ID_PATTERN.matcher(token).find()) {
                matches = true;
                overrideProfileId = token.substring(1);;
            }

            if (!matches) bail();

        }

        this.overrideUserId = overrideUserId;
        this.overrideProfileId = overrideProfileId;

    }

    private void bail() {
        throw new BadSessionSecretException("Invalid SessionSecret Header");
    }

    /**
     * Returns the session secret.  Corresponds to {@link SessionCreation#getSessionSecret()}
     *
     * @return the session secret.
     */
    public String getSessionSecret() {
        return sessionSecret;
    }

    /**
     * Gets the override user ID.  Corresponds to {@link User#getId()}
     *
     * @return the override user ID.
     */
    public String getOverrideUserId() {
        return overrideUserId;
    }

    /**
     * Gets the override profile ID.  Corresponds to {@link Profile#getId()}
     *
     * @return the override profile id.
     */
    public String getOverrideProfileId() {
        return overrideProfileId;
    }

}
