package dev.getelements.elements.service.email;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.service.email.EmailService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class DefaultEmailService implements EmailService {

    private MailSessionProvider mailSessionProvider;
    private String defaultFrom;

    @Override
    public void send(final String from, final String to, final String subject, final String body, final boolean html) {
        final var session = mailSessionProvider.get();
        if (session == null) {
            throw new InvalidDataException("Email service is not configured (SMTP_HOST is blank).");
        }

        final var resolvedFrom = (from == null || from.isBlank()) ? defaultFrom : from;

        try {
            final var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(resolvedFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, html ? "text/html; charset=utf-8" : "text/plain; charset=utf-8");
            Transport.send(message);
        } catch (final MessagingException e) {
            throw new InternalException("Failed to send email.", e);
        }
    }

    public MailSessionProvider getMailSessionProvider() {
        return mailSessionProvider;
    }

    @Inject
    public void setMailSessionProvider(final MailSessionProvider mailSessionProvider) {
        this.mailSessionProvider = mailSessionProvider;
    }

    public String getDefaultFrom() {
        return defaultFrom;
    }

    @Inject
    public void setDefaultFrom(@Named(EmailService.DEFAULT_FROM) final String defaultFrom) {
        this.defaultFrom = defaultFrom;
    }

}
