package dev.getelements.elements.sdk.model.schema.email;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/** Represents a reusable, admin-managed email template used to compose system emails. */
@Schema(description = "Represents a reusable email template.")
public class EmailTemplate implements Serializable {

    /** Creates a new instance. */
    public EmailTemplate() {}

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the email template.")
    private String id;

    @NotNull
    @Schema(description = "The unique key used to reference this email template (e.g. "
            + "dev.getelements.elements.password_reset.email_template).")
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

    @Schema(description = "The time the email template was created.")
    private Date createdAt;

    @Schema(description = "The time the email template was last updated.")
    private Date updatedAt;

    /**
     * Returns the unique ID of the email template.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the email template.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

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

    /**
     * Returns the time the email template was created.
     *
     * @return the createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the time the email template was created.
     *
     * @param createdAt the createdAt
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the time the email template was last updated.
     *
     * @return the updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the time the email template was last updated.
     *
     * @param updatedAt the updatedAt
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailTemplate that = (EmailTemplate) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getKey(), that.getKey()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getSubject(), that.getSubject()) &&
                Objects.equals(getBody(), that.getBody()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), that.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getKey(), getName(), getSubject(), getBody(), getDescription(),
                getCreatedAt(), getUpdatedAt());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EmailTemplate{");
        sb.append("id='").append(id).append('\'');
        sb.append(", key='").append(key).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", body='").append(body).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append('}');
        return sb.toString();
    }

}
