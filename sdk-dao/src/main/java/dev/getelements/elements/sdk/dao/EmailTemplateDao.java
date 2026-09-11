package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.schema.EmailTemplateNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

@ElementServiceExport
@ElementEventProducer(
        value = EmailTemplateDao.EMAIL_TEMPLATE_CREATED,
        parameters = EmailTemplate.class,
        description = "Called when an email template was created."
)
@ElementEventProducer(
        value = EmailTemplateDao.EMAIL_TEMPLATE_CREATED,
        parameters = {EmailTemplate.class, Transaction.class},
        description = "Called when an email template was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = EmailTemplateDao.EMAIL_TEMPLATE_UPDATED,
        parameters = EmailTemplate.class,
        description = "Called when an email template was updated."
)
@ElementEventProducer(
        value = EmailTemplateDao.EMAIL_TEMPLATE_UPDATED,
        parameters = {EmailTemplate.class, Transaction.class},
        description = "Called when an email template was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = EmailTemplateDao.EMAIL_TEMPLATE_DELETED,
        parameters = EmailTemplate.class,
        description = "Called when an email template was deleted."
)
@ElementEventProducer(
        value = EmailTemplateDao.EMAIL_TEMPLATE_DELETED,
        parameters = {EmailTemplate.class, Transaction.class},
        description = "Called when an email template was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface EmailTemplateDao {

    String EMAIL_TEMPLATE_CREATED = "dev.getelements.elements.sdk.model.dao.email.template.created";

    String EMAIL_TEMPLATE_UPDATED = "dev.getelements.elements.sdk.model.dao.email.template.updated";

    String EMAIL_TEMPLATE_DELETED = "dev.getelements.elements.sdk.model.dao.email.template.deleted";

    /**
     * Lists all {@link EmailTemplate} instances.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link EmailTemplate} instances
     */
    Pagination<EmailTemplate> getEmailTemplates(int offset, int count);

    /**
     * Finds an email template by its id.
     *
     * @param id the email template ID
     * @return an {@link Optional} possibly containing the {@link EmailTemplate}
     */
    Optional<EmailTemplate> findEmailTemplate(String id);

    /**
     * Fetches a specific {@link EmailTemplate} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param id the email template ID
     * @return the {@link EmailTemplate}, never null
     */
    default EmailTemplate getEmailTemplate(String id) {
        return findEmailTemplate(id).orElseThrow(EmailTemplateNotFoundException::new);
    }

    /**
     * Finds an email template by its key.
     *
     * @param key the email template key
     * @return an {@link Optional} possibly containing the {@link EmailTemplate}
     */
    Optional<EmailTemplate> findEmailTemplateByKey(String key);

    /**
     * Fetches a specific {@link EmailTemplate} instance based on key. If not found, an
     * exception is raised.
     *
     * @param key the email template key
     * @return the {@link EmailTemplate}, never null
     */
    default EmailTemplate getEmailTemplateByKey(final String key) {
        return findEmailTemplateByKey(key).orElseThrow(EmailTemplateNotFoundException::new);
    }

    /**
     * Creates a new email template.
     *
     * @param emailTemplate
     * @return
     */
    EmailTemplate createEmailTemplate(EmailTemplate emailTemplate);

    /**
     * Updates an existing email template.
     *
     * @param emailTemplate
     * @return
     */
    EmailTemplate updateEmailTemplate(EmailTemplate emailTemplate);

    /**
     * Deletes the {@link EmailTemplate} with the supplied ID.
     *
     * @param id the email template ID.
     */
    void deleteEmailTemplate(String id);

}
