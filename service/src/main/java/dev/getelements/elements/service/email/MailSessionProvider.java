package dev.getelements.elements.service.email;

import jakarta.inject.Provider;
import jakarta.mail.Session;

public interface MailSessionProvider extends Provider<Session> {
}
