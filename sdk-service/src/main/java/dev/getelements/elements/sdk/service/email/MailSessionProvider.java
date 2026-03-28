package dev.getelements.elements.sdk.service.email;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import jakarta.inject.Provider;
import jakarta.mail.Session;

/**
 * Provides a {@link Session} for sending email via SMTP. Rebind this interface in a custom Guice module to override
 * the default SMTP provider.
 */
@ElementPublic
public interface MailSessionProvider extends Provider<Session> {
}
