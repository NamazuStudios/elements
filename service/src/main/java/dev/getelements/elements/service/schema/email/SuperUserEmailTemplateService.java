package dev.getelements.elements.service.schema.email;

import dev.getelements.elements.sdk.dao.EmailTemplateDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.schema.EmailTemplateNotFoundException;
import dev.getelements.elements.sdk.model.schema.email.CreateEmailTemplateRequest;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.model.schema.email.UpdateEmailTemplateRequest;
import dev.getelements.elements.sdk.service.schema.email.EmailTemplateService;
import dev.getelements.elements.sdk.service.user.EmailVerificationService;
import dev.getelements.elements.sdk.service.user.PasswordResetService;
import jakarta.inject.Inject;

public class SuperUserEmailTemplateService implements EmailTemplateService {

    /** Reserved key prefix for core, built-in email templates. Not creatable or deletable via the API. */
    private static final String RESERVED_PREFIX = "dev.getelements.elements.";

    private static final String RESET_EMAIL_TEMPLATE_NAME = "Password Reset Email";

    private static final String RESET_EMAIL_SUBJECT_DEFAULT = "Reset your password";

    private static final String RESET_EMAIL_BODY_DEFAULT =
            "<p>Click the link below to reset your password. This link expires in 1 hour.</p>"
          + "<p><a href=\"{link}\">Reset Password</a></p>";

    private static final String VERIFICATION_EMAIL_TEMPLATE_NAME = "Email Verification Email";

    private static final String VERIFICATION_EMAIL_SUBJECT_DEFAULT = "Verify your email";

    private static final String VERIFICATION_EMAIL_BODY_DEFAULT =
            "<p>Please verify your email address by clicking the link below:</p>"
          + "<p><a href=\"{link}\">Verify Email</a></p>";

    private EmailTemplateDao emailTemplateDao;

    @Override
    public Pagination<EmailTemplate> getEmailTemplates(final int offset, final int count) {

        // Ensure the core, built-in templates always exist so they show up in the admin list even
        // before any email of that kind has ever been sent.
        getOrCreateEmailTemplate(
                PasswordResetService.RESET_EMAIL_TEMPLATE,
                RESET_EMAIL_TEMPLATE_NAME,
                RESET_EMAIL_SUBJECT_DEFAULT,
                RESET_EMAIL_BODY_DEFAULT);

        getOrCreateEmailTemplate(
                EmailVerificationService.VERIFICATION_EMAIL_TEMPLATE,
                VERIFICATION_EMAIL_TEMPLATE_NAME,
                VERIFICATION_EMAIL_SUBJECT_DEFAULT,
                VERIFICATION_EMAIL_BODY_DEFAULT);

        return getEmailTemplateDao().getEmailTemplates(offset, count);
    }

    @Override
    public EmailTemplate getEmailTemplate(final String idOrKey) {
        return getEmailTemplateDao()
                .findEmailTemplate(idOrKey)
                .or(() -> getEmailTemplateDao().findEmailTemplateByKey(idOrKey))
                .orElseThrow(EmailTemplateNotFoundException::new);
    }

    @Override
    public EmailTemplate createEmailTemplate(final CreateEmailTemplateRequest request) {

        if (request.getKey() != null && request.getKey().startsWith(RESERVED_PREFIX)) {
            throw new InvalidDataException("Email template key may not use the reserved prefix: " + RESERVED_PREFIX);
        }

        final var template = new EmailTemplate();
        template.setKey(request.getKey());
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setDescription(request.getDescription());

        return getEmailTemplateDao().createEmailTemplate(template);
    }

    @Override
    public EmailTemplate updateEmailTemplate(final String id, final UpdateEmailTemplateRequest request) {
        final var template = getEmailTemplateDao().getEmailTemplate(id);
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setDescription(request.getDescription());
        return getEmailTemplateDao().updateEmailTemplate(template);
    }

    @Override
    public void deleteEmailTemplate(final String id) {

        final var template = getEmailTemplateDao().getEmailTemplate(id);

        if (template.getKey() != null && template.getKey().startsWith(RESERVED_PREFIX)) {
            throw new InvalidDataException("Core email templates may not be deleted: " + template.getKey());
        }

        getEmailTemplateDao().deleteEmailTemplate(id);
    }

    @Override
    public EmailTemplate getOrCreateEmailTemplate(final String key,
                                                   final String defaultName,
                                                   final String defaultSubject,
                                                   final String defaultBody) {
        return getEmailTemplateDao().findEmailTemplateByKey(key).orElseGet(() -> {
            final var template = new EmailTemplate();
            template.setKey(key);
            template.setName(defaultName);
            template.setSubject(defaultSubject);
            template.setBody(defaultBody);
            return getEmailTemplateDao().createEmailTemplate(template);
        });
    }

    public EmailTemplateDao getEmailTemplateDao() {
        return emailTemplateDao;
    }

    @Inject
    public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
        this.emailTemplateDao = emailTemplateDao;
    }

}
