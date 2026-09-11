package dev.getelements.elements.sdk.model.schema.email;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/** Represents a request to create an {@link EmailTemplate}. */
@Schema(description = "Represents a request to create an EmailTemplate.")
public class CreateEmailTemplateRequest implements Serializable {

    /** Creates a new instance. */
    public CreateEmailTemplateRequest() {}

    @NotNull
    @Schema(description = "The unique key used to reference this email template.")
    private String key;

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
     * Returns the unique key of the email template.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the unique key of the email template.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

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
        CreateEmailTemplateRequest that = (CreateEmailTemplateRequest) o;
        return Objects.equals(getKey(), that.getKey()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getSubject(), that.getSubject()) &&
                Objects.equals(getBody(), that.getBody()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getName(), getSubject(), getBody(), getDescription());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateEmailTemplateRequest{");
        sb.append("key='").append(key).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", body='").append(body).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
