package dev.getelements.elements.service.schema.email;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.EmailTemplateDao;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.schema.email.CreateEmailTemplateRequest;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class SuperUserEmailTemplateServiceTest {

    @Inject
    private SuperUserEmailTemplateService service;

    @Inject
    private EmailTemplateDao emailTemplateDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    @Test(expectedExceptions = InvalidDataException.class)
    public void testCreateRejectsReservedKeyPrefix() {
        final var request = new CreateEmailTemplateRequest();
        request.setKey("dev.getelements.elements.some_core_template");
        request.setName("Reserved");
        request.setSubject("Subject");
        request.setBody("Body");
        service.createEmailTemplate(request);
    }

    @Test
    public void testCreateAllowsNonReservedKeyPrefix() {

        when(emailTemplateDao.createEmailTemplate(any())).thenAnswer(i -> i.getArgument(0));

        final var request = new CreateEmailTemplateRequest();
        request.setKey("com.example.my_template");
        request.setName("My Template");
        request.setSubject("Subject");
        request.setBody("Body");

        final var created = service.createEmailTemplate(request);

        assertEquals(created.getKey(), "com.example.my_template");
        verify(emailTemplateDao).createEmailTemplate(any());
    }

    @Test(expectedExceptions = InvalidDataException.class)
    public void testDeleteRejectsReservedKeyPrefix() {

        final var existing = new EmailTemplate();
        existing.setId("template-1");
        existing.setKey("dev.getelements.elements.password_reset.email_template");
        when(emailTemplateDao.getEmailTemplate("template-1")).thenReturn(existing);

        service.deleteEmailTemplate("template-1");
    }

    @Test
    public void testDeleteAllowsNonReservedKeyPrefix() {

        final var existing = new EmailTemplate();
        existing.setId("template-1");
        existing.setKey("com.example.my_template");
        when(emailTemplateDao.getEmailTemplate("template-1")).thenReturn(existing);

        service.deleteEmailTemplate("template-1");

        verify(emailTemplateDao).deleteEmailTemplate("template-1");
    }

    @Test
    public void testGetOrCreateEmailTemplateReturnsExistingWithoutCreating() {

        final var existing = new EmailTemplate();
        existing.setId("template-1");
        existing.setKey("dev.getelements.elements.password_reset.email_template");
        existing.setName("Existing");
        existing.setSubject("Existing Subject");
        existing.setBody("Existing Body");

        when(emailTemplateDao.findEmailTemplateByKey("dev.getelements.elements.password_reset.email_template"))
                .thenReturn(Optional.of(existing));

        final var result = service.getOrCreateEmailTemplate(
                "dev.getelements.elements.password_reset.email_template",
                "Default Name", "Default Subject", "Default Body");

        assertEquals(result, existing);
        verify(emailTemplateDao, never()).createEmailTemplate(any());
    }

    @Test
    public void testGetOrCreateEmailTemplateCreatesWithDefaultsWhenAbsent() {

        when(emailTemplateDao.findEmailTemplateByKey("dev.getelements.elements.verification.email_template"))
                .thenReturn(Optional.empty());
        when(emailTemplateDao.createEmailTemplate(any())).thenAnswer(i -> i.getArgument(0));

        final var result = service.getOrCreateEmailTemplate(
                "dev.getelements.elements.verification.email_template",
                "Email Verification Email", "Verify your email", "<p>Body</p>");

        final var captor = ArgumentCaptor.forClass(EmailTemplate.class);
        verify(emailTemplateDao).createEmailTemplate(captor.capture());

        assertEquals(captor.getValue().getKey(), "dev.getelements.elements.verification.email_template");
        assertEquals(captor.getValue().getName(), "Email Verification Email");
        assertEquals(captor.getValue().getSubject(), "Verify your email");
        assertEquals(captor.getValue().getBody(), "<p>Body</p>");
        assertEquals(result.getKey(), "dev.getelements.elements.verification.email_template");
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EmailTemplateDao.class).toInstance(mock(EmailTemplateDao.class));
        }

    }

}
