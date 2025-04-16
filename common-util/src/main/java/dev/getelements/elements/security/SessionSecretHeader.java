package dev.getelements.elements.security;

import dev.getelements.elements.sdk.model.Headers;
import dev.getelements.elements.sdk.model.exception.security.BadSessionSecretException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.util.security.HeaderOptionalSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.model.Headers.SOCIALENGINE_SESSION_SECRET;
import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;

/**
 * Parses the value in the session secret header specified in {@link Headers#SESSION_SECRET}.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SessionSecretHeader {

    private static final Logger logger = LoggerFactory.getLogger(SessionSecretHeader.class);

    private static final Pattern SEPARATOR = compile("\\s+");

    private static final Pattern SECRET_PATTERN = compile("\\w+");

    private static final Pattern OVERRIDE_USER_ID_PATTERN = compile("u\\w+");

    private static final Pattern OVERRIDE_PROFILE_ID_PATTERN = compile("p\\w+");

    private final Optional<String> sessionSecret;

    private final Optional<String> overrideUserId;

    private final Optional<String> overrideProfileId;

    public static final <T> SessionSecretHeader withValueSupplier(final Function<String, T> tValueSupplier) {
        return new SessionSecretHeader(getSessionSecretHeader(tValueSupplier));
    }

    public static final <T> SessionSecretHeader withOptionalValueSupplier(final HeaderOptionalSupplier<T> tOptionalSupplier) {
        return new SessionSecretHeader(getOptionalSessionSecretHeader(tOptionalSupplier));
    }

    @SuppressWarnings("deprecated") // This is here to provide backwards compatibility.
    private static <T> Optional<String> getOptionalSessionSecretHeader(final HeaderOptionalSupplier<T> tOptionalSupplier) {
        final Optional<String> secret = tOptionalSupplier.asString(SESSION_SECRET);
        return secret.isPresent() ? secret : tOptionalSupplier.asString(SOCIALENGINE_SESSION_SECRET);
    }

    @SuppressWarnings("deprecated") // This is here to provide backwards compatibility.
    private static <T> Optional<String> getSessionSecretHeader(final Function<String, T> tFunction) {
        String header = getHeader(tFunction, SESSION_SECRET);
        header = header == null ? getHeader(tFunction, SOCIALENGINE_SESSION_SECRET) : header;
        return header == null ? Optional.empty() : Optional.of(header);
    }

    private static <T> String getHeader(final Function<String, T> headerSupplierFunction, final String header) {
        try  {
            return (String) headerSupplierFunction.apply(header);
        } catch (ClassCastException ex) {
            logger.warn("Fetched non-string header for header {}", header);
            return null;
        }
    }

    /**
     * Parses out the session secret header and validates all tokens therein.  This will raise an instance of
     * {@link BadSessionSecretException} in the event of parse failure.
     *
     * @param header the header value, may be null
     */

    private SessionSecretHeader(final Optional<String> header) {

        if (header.isEmpty()) {
            sessionSecret = Optional.empty();
            overrideUserId = Optional.empty();
            overrideProfileId = Optional.empty();
            return;
        }

        final var tokens = Stream.of(SEPARATOR.split(header.get()))
                .filter(not(String::isBlank))
                .map(String::trim)
                .iterator();

        if (!tokens.hasNext()) bail();

        sessionSecret = Optional.of(tokens.next());
        if (!SECRET_PATTERN.matcher(sessionSecret.get()).find()) bail();

        Optional<String> overrideUserId = Optional.empty();
        Optional<String> overrideProfileId = Optional.empty();

        while (tokens.hasNext()) {

            boolean matches = false;

            final var token = tokens.next();

            if (OVERRIDE_USER_ID_PATTERN.matcher(token).find()) {
                matches = true;
                overrideUserId = Optional.of(token.substring(1));
            } else if (OVERRIDE_PROFILE_ID_PATTERN.matcher(token).find()) {
                matches = true;
                overrideProfileId = Optional.of(token.substring(1));
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
    public Optional<String> getSessionSecret() {
        return sessionSecret;
    }

    /**
     * Gets the override user ID.  Corresponds to {@link User#getId()}
     *
     * @return the override user ID.
     */
    public Optional<String> getOverrideUserId() {
        return overrideUserId;
    }

    /**
     * Gets the override profile ID.  Corresponds to {@link Profile#getId()}
     *
     * @return the override profile id.
     */
    public Optional<String> getOverrideProfileId() {
        return overrideProfileId;
    }

}
