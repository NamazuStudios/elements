/**
 * Platform email service implementation.
 *
 * <p>Guice bindings expose the following services:
 * <ul>
 *   <li>{@link dev.getelements.elements.sdk.service.email.EmailService} — send transactional email.</li>
 *   <li>{@link jakarta.mail.Session} — the underlying Jakarta Mail session, for callers that need
 *       direct access to the transport (e.g. advanced MIME construction).</li>
 * </ul>
 *
 * <p>Both are provided by {@link dev.getelements.elements.service.email.SmtpMailSessionProvider}.
 * Inject {@code Provider<Session>} rather than {@code Session} directly if you need lazy
 * resolution (e.g. in code paths where email may not always be needed).
 */
package dev.getelements.elements.service.email;
