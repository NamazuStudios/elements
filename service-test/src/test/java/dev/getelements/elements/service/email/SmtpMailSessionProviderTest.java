package dev.getelements.elements.service.email;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SmtpMailSessionProviderTest {

    private SmtpMailSessionProvider provider(String host) {
        final var p = new SmtpMailSessionProvider();
        p.setSmtpHost(host);
        p.setSmtpPort("587");
        p.setSmtpUser("user");
        p.setSmtpPassword("pass");
        p.setSmtpStarttls("true");
        return p;
    }

    @Test
    public void testGet_blankHost_throwsInvalidDataException() {
        assertThrows(InvalidDataException.class, () -> provider("").get());
    }

    @Test
    public void testGet_whitespaceHost_throwsInvalidDataException() {
        assertThrows(InvalidDataException.class, () -> provider("   ").get());
    }

    @Test
    public void testGet_configuredHost_returnsSession() {
        final var session = provider("smtp.example.com").get();
        assertNotNull(session);
        assertEquals(session.getProperty("mail.smtp.host"), "smtp.example.com");
        assertEquals(session.getProperty("mail.smtp.port"), "587");
        assertEquals(session.getProperty("mail.smtp.starttls.enable"), "true");
        assertEquals(session.getProperty("mail.smtp.auth"), "true");
    }

}
