package dev.getelements.elements.dao.mongo.schema.email;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.EmailTemplateDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.schema.email.MongoEmailTemplate;
import dev.getelements.elements.sdk.model.exception.schema.EmailTemplateNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;

import jakarta.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoEmailTemplateDao implements EmailTemplateDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    private ValidationHelper validationHelper;

    private Consumer<Event> eventPublisher;

    @Override
    public Pagination<EmailTemplate> getEmailTemplates(final int offset, final int count) {

        final var mongoQuery = getDatastore()
                .find(MongoEmailTemplate.class)
                .filter(exists("key"));

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform);

    }

    @Override
    public Optional<EmailTemplate> findEmailTemplate(final String id) {
        return findMongoEmailTemplate(id).map(this::transform);
    }

    public Optional<MongoEmailTemplate> findMongoEmailTemplate(final String id) {
        return getMongoDBUtils()
                .parse(id)
                .flatMap(objectId -> Optional.ofNullable(getDatastore()
                        .find(MongoEmailTemplate.class)
                        .filter(eq("_id", objectId), exists("key"))
                        .first()));
    }

    @Override
    public Optional<EmailTemplate> findEmailTemplateByKey(final String key) {
        return findMongoEmailTemplateByKey(key).map(this::transform);
    }

    public Optional<MongoEmailTemplate> findMongoEmailTemplateByKey(final String key) {

        final var template = getDatastore()
                .find(MongoEmailTemplate.class)
                .filter(eq("key", key))
                .first();

        return Optional.ofNullable(template);

    }

    @Override
    public EmailTemplate createEmailTemplate(final EmailTemplate emailTemplate) {

        getValidationHelper().validateModel(emailTemplate, ValidationGroups.Insert.class);

        final var now = new Date();

        final var toInsert = getBeanMapper().map(emailTemplate, MongoEmailTemplate.class);
        toInsert.setCreatedAt(now);
        toInsert.setUpdatedAt(now);

        final var inserted = getMongoDBUtils().perform(ds -> ds.save(toInsert));
        final var createdTemplate = transform(inserted);

        getEventPublisher().accept(Event.builder()
                .argument(createdTemplate)
                .named(EMAIL_TEMPLATE_CREATED)
                .build());

        return createdTemplate;
    }

    @Override
    public EmailTemplate updateEmailTemplate(final EmailTemplate emailTemplate) {

        getValidationHelper().validateModel(emailTemplate, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
                emailTemplate.getId(),
                EmailTemplateNotFoundException::new
        );

        final var options = new ModifyOptions()
                .upsert(false)
                .returnDocument(AFTER);

        final var updated = getDatastore().find(MongoEmailTemplate.class)
                .filter(eq("_id", objectId), exists("key"))
                .modify(options,
                        set("name", emailTemplate.getName()),
                        set("subject", emailTemplate.getSubject()),
                        set("body", emailTemplate.getBody()),
                        set("description", emailTemplate.getDescription() == null ? "" : emailTemplate.getDescription()),
                        set("updatedAt", new Date())
                );

        if (updated == null) {
            throw new EmailTemplateNotFoundException("Email template with id not found.");
        }

        final var updatedTemplate = transform(updated);

        getEventPublisher().accept(Event.builder()
                .argument(updatedTemplate)
                .named(EMAIL_TEMPLATE_UPDATED)
                .build());

        return updatedTemplate;

    }

    @Override
    public void deleteEmailTemplate(final String id) {

        final var objectId = getMongoDBUtils().parseOrThrow(id, EmailTemplateNotFoundException::new);

        final var query = getDatastore()
                .find(MongoEmailTemplate.class)
                .filter(
                        exists("key"),
                        eq("_id", objectId)
                );

        final var existing = query.first();

        final var result = new UpdateBuilder()
                .with(unset("key"))
                .execute(query, new UpdateOptions().upsert(false));

        if (result.getModifiedCount() == 0) {
            throw new EmailTemplateNotFoundException();
        }

        if (existing != null) {
            getEventPublisher().accept(Event.builder()
                    .argument(transform(existing))
                    .named(EMAIL_TEMPLATE_DELETED)
                    .build());
        }

    }

    public EmailTemplate transform(final MongoEmailTemplate mongoEmailTemplate) {
        return getBeanMapper().map(mongoEmailTemplate, EmailTemplate.class);
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public Consumer<Event> getEventPublisher() {
        return eventPublisher;
    }

    @Inject
    public void setEventPublisher(Consumer<Event> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

}
