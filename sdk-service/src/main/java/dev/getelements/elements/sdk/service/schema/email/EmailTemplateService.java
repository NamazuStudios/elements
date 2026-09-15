package dev.getelements.elements.sdk.service.schema.email;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.schema.email.CreateEmailTemplateRequest;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.model.schema.email.UpdateEmailTemplateRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages instances of {@link EmailTemplate}.
 *
 * <p>Kept as a standalone type (not extending any other service interface) so that the
 * Guice-HK2 bridge can resolve it without type-hierarchy ambiguity.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface EmailTemplateService {

    /**
     * Lists all {@link EmailTemplate} instances.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link EmailTemplate} instances
     */
    Pagination<EmailTemplate> getEmailTemplates(int offset, int count);

    /**
     * Fetches a specific {@link EmailTemplate} instance based on ID or key. If not found, an
     * exception is raised.
     *
     * @param idOrKey the email template ID or key
     * @return the {@link EmailTemplate}, never null
     */
    EmailTemplate getEmailTemplate(String idOrKey);

    /**
     * Creates a new {@link EmailTemplate}.
     *
     * <p>Rejects requests whose key begins with the reserved {@code dev.getelements.elements.} prefix,
     * which is reserved for core system templates.
     *
     * @param request the {@link CreateEmailTemplateRequest} with the information to create
     * @return the {@link EmailTemplate} as it was created by the service.
     */
    EmailTemplate createEmailTemplate(CreateEmailTemplateRequest request);

    /**
     * Updates the {@link EmailTemplate} with the supplied id.
     *
     * @param id      the id of the email template to update
     * @param request the information to update
     * @return the {@link EmailTemplate} as it was changed by the service.
     */
    EmailTemplate updateEmailTemplate(String id, UpdateEmailTemplateRequest request);

    /**
     * Deletes the {@link EmailTemplate} with the supplied id.
     *
     * <p>Rejects requests to delete a template whose key begins with the reserved
     * {@code dev.getelements.elements.} prefix.
     *
     * @param id the email template id.
     */
    void deleteEmailTemplate(String id);

    /**
     * Idempotent upsert — finds an {@link EmailTemplate} by its key, creating it with the supplied
     * defaults if it does not already exist. Core services and custom Elements alike should call this
     * at startup (or lazily, before sending an email) to self-register and fetch their own templates.
     * This method is not subject to the reserved-key restriction that governs
     * {@link #createEmailTemplate(CreateEmailTemplateRequest)} and {@link #deleteEmailTemplate(String)}.
     *
     * @param key            the unique key identifying the template
     * @param defaultName    the display name to use if the template does not already exist
     * @param defaultSubject the subject to use if the template does not already exist
     * @param defaultBody    the HTML body to use if the template does not already exist
     * @return the existing or newly-created {@link EmailTemplate}
     */
    EmailTemplate getOrCreateEmailTemplate(String key, String defaultName, String defaultSubject, String defaultBody);

}
