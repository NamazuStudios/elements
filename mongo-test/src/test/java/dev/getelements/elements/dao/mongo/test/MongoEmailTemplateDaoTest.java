package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.EmailTemplateDao;
import dev.getelements.elements.sdk.model.exception.schema.EmailTemplateNotFoundException;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Named;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.dao.EmailTemplateDao.*;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoEmailTemplateDaoTest {

    private EmailTemplate working;

    private EmailTemplateDao emailTemplateDao;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    private final Set<String> createdEventIds = ConcurrentHashMap.newKeySet();

    private final Set<String> updatedEventIds = ConcurrentHashMap.newKeySet();

    private final Set<String> deletedEventIds = ConcurrentHashMap.newKeySet();

    @BeforeClass
    public void setupEventHandlers() {
        elementRegistry.onEvent(ev -> {
            switch (ev.getEventName()) {
                case EMAIL_TEMPLATE_CREATED -> createdEventIds.add(ev.getEventArgument(0, EmailTemplate.class).getId());
                case EMAIL_TEMPLATE_UPDATED -> updatedEventIds.add(ev.getEventArgument(0, EmailTemplate.class).getId());
                case EMAIL_TEMPLATE_DELETED -> deletedEventIds.add(ev.getEventArgument(0, EmailTemplate.class).getId());
            }
        });
    }

    @Test(groups = "create")
    public void testCreateEmailTemplate() {

        final var template = new EmailTemplate();
        template.setKey("com.example.test." + UUID.randomUUID());
        template.setName("Test Template");
        template.setSubject("Test Subject");
        template.setBody("<p>Test Body</p>");
        template.setDescription("A test template.");

        working = getEmailTemplateDao().createEmailTemplate(template);

        assertNotNull(working.getId());
        assertEquals(working.getKey(), template.getKey());
        assertEquals(working.getName(), template.getName());
        assertEquals(working.getSubject(), template.getSubject());
        assertEquals(working.getBody(), template.getBody());

        assertTrue(createdEventIds.contains(working.getId()),
                "Expected EMAIL_TEMPLATE_CREATED event for " + working.getId());

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateEmailTemplate() {

        working.setName("Updated Name");
        working.setSubject("Updated Subject");
        working.setBody("<p>Updated Body</p>");

        final var updated = getEmailTemplateDao().updateEmailTemplate(working);
        assertEquals(updated.getName(), "Updated Name");
        assertEquals(updated.getSubject(), "Updated Subject");
        assertEquals(updated.getBody(), "<p>Updated Body</p>");
        working = updated;

        assertTrue(updatedEventIds.contains(working.getId()),
                "Expected EMAIL_TEMPLATE_UPDATED event for " + working.getId());

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetSingleById() {
        final var fetched = getEmailTemplateDao().getEmailTemplate(working.getId());
        assertEquals(fetched.getId(), working.getId());
    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetSingleByKey() {
        final var fetched = getEmailTemplateDao().getEmailTemplateByKey(working.getKey());
        assertEquals(fetched.getId(), working.getId());
    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMultiple() {
        final var templates = new PaginationWalker().toList(getEmailTemplateDao()::getEmailTemplates);
        assertTrue(templates.stream().anyMatch(t -> t.getId().equals(working.getId())));
    }

    @Test(groups = "delete", dependsOnGroups = "fetch")
    public void testDelete() {
        getEmailTemplateDao().deleteEmailTemplate(working.getId());
        assertTrue(deletedEventIds.contains(working.getId()),
                "Expected EMAIL_TEMPLATE_DELETED event for " + working.getId());
    }

    @Test(groups = "delete",
            dependsOnMethods = "testDelete",
            expectedExceptions = EmailTemplateNotFoundException.class)
    public void testDoubleDelete() {
        getEmailTemplateDao().deleteEmailTemplate(working.getId());
    }

    @Test(groups = "delete",
            dependsOnMethods = "testDelete",
            expectedExceptions = EmailTemplateNotFoundException.class)
    public void testTemplateIsDeleted() {
        getEmailTemplateDao().getEmailTemplate(working.getId());
    }

    @Test(expectedExceptions = EmailTemplateNotFoundException.class)
    public void testEmailTemplateNotFoundById() {
        final var objectId = new ObjectId();
        getEmailTemplateDao().getEmailTemplate(objectId.toString());
    }

    public EmailTemplateDao getEmailTemplateDao() {
        return emailTemplateDao;
    }

    @Inject
    public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
        this.emailTemplateDao = emailTemplateDao;
    }

}
