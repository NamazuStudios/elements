package dev.getelements.elements.service.email;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.email.EmailService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class DefaultEmailService implements EmailService {

    private Provider<Session> sessionProvider;
    private String defaultFrom;

    @Override
    public void send(final String from, final String to, final String subject, final String body, final boolean html) {

        final var resolvedFrom = (from == null || from.isBlank()) ? defaultFrom : from;

        try {
            final var message = new MimeMessage(sessionProvider.get());
            message.setFrom(new InternetAddress(resolvedFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, html ? "text/html; charset=utf-8" : "text/plain; charset=utf-8");

            Transport.send(message);

        } catch (final MessagingException e) {
            throw new InternalException("Failed to send email.", e);
        }
    }

    public Provider<Session> getSessionProvider() {
        return sessionProvider;
    }

    @Inject
    public void setSessionProvider(final Provider<Session> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public String getDefaultFrom() {
        return defaultFrom;
    }

    @Inject
    public void setDefaultFrom(@Named(EmailService.DEFAULT_FROM) final String defaultFrom) {
        this.defaultFrom = defaultFrom;
    }

}
