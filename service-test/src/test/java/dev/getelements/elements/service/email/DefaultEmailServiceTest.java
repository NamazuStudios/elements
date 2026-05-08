package dev.getelements.elements.service.email;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import jakarta.inject.Provider;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class DefaultEmailServiceTest {

    private static final String FROM    = "sender@example.com";
    private static final String TO      = "recipient@example.com";
    private static final String SUBJECT = "Test subject";
    private static final String BODY    = "Hello, world!";

    private static final String SMTP_USER = "testuser";
    private static final String SMTP_PASS = "testpass";

    // A fresh GreenMail instance per test avoids port/state issues with reset().
    private GreenMail greenMail;
    private int smtpPort;

    @BeforeMethod
    public void startMailServer() {
        final var setup = new ServerSetup(0, "localhost", ServerSetup.PROTOCOL_SMTP);
        greenMail = new GreenMail(setup);
        greenMail.start();
        smtpPort = greenMail.getSmtp().getPort();
        greenMail.setUser(FROM, SMTP_USER, SMTP_PASS);
    }

    @AfterMethod(alwaysRun = true)
    public void stopMailServer() {
        if (greenMail != null) greenMail.stop();
    }

    // ---------- unit tests (no SMTP) ----------

    @Test
    @SuppressWarnings("unchecked")
    public void testSend_providerThrows_propagatesException() {
        final Provider<Session> provider = mock(Provider.class);
        when(provider.get()).thenThrow(new InvalidDataException("Email service is not configured (SMTP_HOST is blank)."));

        final var service = new DefaultEmailService();
        service.setSessionProvider(provider);
        service.setDefaultFrom(FROM);
        service.setSmtpHost("localhost");

        assertThrows(InvalidDataException.class,
                () -> service.send(FROM, TO, SUBJECT, BODY, false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSend_blankSmtpHost_throwsInvalidDataException() {
        final var service = new DefaultEmailService();
        service.setSessionProvider(mock(Provider.class));
        service.setDefaultFrom(FROM);
        service.setSmtpHost("");

        try {
            service.send(FROM, TO, SUBJECT, BODY, false);
            fail("Expected InvalidDataException");
        } catch (final InvalidDataException ex) {
            assertTrue(ex.getMessage().contains("SMTP_HOST"), "Expected message to mention SMTP_HOST");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSend_nullSmtpHost_throwsInvalidDataException() {
        final var service = new DefaultEmailService();
        service.setSessionProvider(mock(Provider.class));
        service.setDefaultFrom(FROM);
        service.setSmtpHost(null);

        try {
            service.send(FROM, TO, SUBJECT, BODY, false);
            fail("Expected InvalidDataException");
        } catch (final InvalidDataException ex) {
            assertTrue(ex.getMessage().contains("SMTP_HOST"), "Expected message to mention SMTP_HOST");
        }
    }

    // ---------- GreenMail integration tests ----------

    @Test
    public void testSend_plainText_delivered() throws Exception {
        buildService(FROM).send(FROM, TO, SUBJECT, BODY, false);

        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(messages.length, 1);
        assertEquals(messages[0].getSubject(), SUBJECT);
        assertTrue(messages[0].getContentType().startsWith("text/plain"),
                "Expected text/plain but got: " + messages[0].getContentType());
        assertTrue(messages[0].getContent().toString().contains(BODY));
    }

    @Test
    public void testSend_html_delivered() throws Exception {
        final var htmlBody = "<h1>Hello</h1>";
        buildService(FROM).send(FROM, TO, SUBJECT, htmlBody, true);

        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(messages.length, 1);
        assertTrue(messages[0].getContentType().startsWith("text/html"),
                "Expected text/html but got: " + messages[0].getContentType());
        assertTrue(messages[0].getContent().toString().contains(htmlBody));
    }

    @Test
    public void testSend_nullFrom_fallsBackToDefaultFrom() throws Exception {
        buildService(FROM).send(null, TO, SUBJECT, BODY, false);

        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(messages.length, 1);
        assertEquals(messages[0].getFrom()[0].toString(), FROM);
    }

    @Test
    public void testSend_blankFrom_fallsBackToDefaultFrom() throws Exception {
        buildService(FROM).send("   ", TO, SUBJECT, BODY, false);

        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(messages.length, 1);
        assertEquals(messages[0].getFrom()[0].toString(), FROM);
    }

    // ---------- helpers ----------

    private DefaultEmailService buildService(final String defaultFrom) {
        final var provider = new SmtpMailSessionProvider();
        provider.setSmtpHost("localhost");
        provider.setSmtpPort(String.valueOf(smtpPort));
        provider.setSmtpUser(SMTP_USER);
        provider.setSmtpPassword(SMTP_PASS);
        provider.setSmtpStarttls("false");

        final var service = new DefaultEmailService();
        service.setSessionProvider(provider);
        service.setDefaultFrom(defaultFrom);
        service.setSmtpHost("localhost");
        return service;
    }

}
