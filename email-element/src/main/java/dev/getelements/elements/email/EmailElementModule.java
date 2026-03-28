package dev.getelements.elements.email;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.service.email.EmailService;
import dev.getelements.elements.sdk.service.email.MailSessionProvider;
import dev.getelements.elements.service.email.DefaultEmailService;
import dev.getelements.elements.service.email.SmtpMailSessionProvider;

public class EmailElementModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MailSessionProvider.class).to(SmtpMailSessionProvider.class);
        bind(EmailService.class).to(DefaultEmailService.class);
    }

}
