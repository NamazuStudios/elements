package dev.getelements.elements.sdk.service.email;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Sends transactional email via SMTP. SMTP settings are read from element attributes, allowing each element to have
 * independently configured mail transport.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface EmailService {

    /** SMTP host. Leave blank to disable email. */
    @ElementDefaultAttribute(value = "", description = "SMTP host. Leave blank to disable email.")
    String SMTP_HOST = "dev.getelements.elements.email.smtp.host";

    /** SMTP port. */
    @ElementDefaultAttribute(value = "587", description = "SMTP port.")
    String SMTP_PORT = "dev.getelements.elements.email.smtp.port";

    /** Enable SMTP STARTTLS. */
    @ElementDefaultAttribute(value = "true", description = "Enable SMTP STARTTLS.")
    String SMTP_STARTTLS = "dev.getelements.elements.email.smtp.starttls";

    /** SMTP username. */
    @ElementDefaultAttribute(value = "", sensitive = true, description = "SMTP username.")
    String SMTP_USER = "dev.getelements.elements.email.smtp.user";

    /** SMTP password. */
    @ElementDefaultAttribute(value = "", sensitive = true, description = "SMTP password.")
    String SMTP_PASSWORD = "dev.getelements.elements.email.smtp.password";

    /** Default From address. */
    @ElementDefaultAttribute(value = "", description = "Default From address.")
    String DEFAULT_FROM = "dev.getelements.elements.email.default.from";

    /**
     * Sends an email.
     *
     * @param from    sender address; if null or blank, falls back to {@link #DEFAULT_FROM}
     * @param to      recipient address
     * @param subject subject line
     * @param body    message body
     * @param html    {@code true} for {@code text/html}, {@code false} for {@code text/plain}
     */
    void send(String from, String to, String subject, String body, boolean html);

}
