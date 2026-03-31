package dev.getelements.elements.service.email;

import dev.getelements.elements.sdk.service.email.EmailService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SmtpMailSessionProvider implements MailSessionProvider {

    private static final Logger logger = LoggerFactory.getLogger(SmtpMailSessionProvider.class);

    private String smtpHost;
    private String smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private String smtpStarttls;

    @Override
    public Session get() {
        if (smtpHost == null || smtpHost.isBlank()) {
            logger.warn("Email disabled: SMTP_HOST is blank.");
            return null;
        }

        final var props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", smtpStarttls);

        final var user = smtpUser;
        final var password = smtpPassword;

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    @Inject
    public void setSmtpHost(@Named(EmailService.SMTP_HOST) final String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    @Inject
    public void setSmtpPort(@Named(EmailService.SMTP_PORT) final String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    @Inject
    public void setSmtpUser(@Named(EmailService.SMTP_USER) final String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    @Inject
    public void setSmtpPassword(@Named(EmailService.SMTP_PASSWORD) final String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getSmtpStarttls() {
        return smtpStarttls;
    }

    @Inject
    public void setSmtpStarttls(@Named(EmailService.SMTP_STARTTLS) final String smtpStarttls) {
        this.smtpStarttls = smtpStarttls;
    }

}
