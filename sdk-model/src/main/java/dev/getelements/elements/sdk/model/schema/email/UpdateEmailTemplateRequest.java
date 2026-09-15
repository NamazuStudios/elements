package dev.getelements.elements.sdk.model.schema.email;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/** Represents a request to update an {@link EmailTemplate}'s name, subject, body, and description. */
@Schema(description = "Represents a request to update an EmailTemplate.")
public class UpdateEmailTemplateRequest implements Serializable {

    /** Creates a new instance. */
    public UpdateEmailTemplateRequest() {}

    @NotNull
    @Schema(description = "The display name of the email template.")
    private String name;

    @NotNull
    @Schema(description = "The subject line of the email template.")
    private String subject;

    @NotNull
    @Schema(description = "The HTML body of the email template.")
    private String body;

    @Schema(description = "An optional human-readable description of the email template.")
    private String description;

    /**
     * Returns the display name of the email template.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the email template.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the subject line of the email template.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject line of the email template.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the HTML body of the email template.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the HTML body of the email template.
     *
     * @param body the body
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns the description of the email template.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the email template.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateEmailTemplateRequest that = (UpdateEmailTemplateRequest) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getSubject(), that.getSubject()) &&
                Objects.equals(getBody(), that.getBody()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSubject(), getBody(), getDescription());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateEmailTemplateRequest{");
        sb.append("name='").append(name).append('\'');
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", body='").append(body).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
