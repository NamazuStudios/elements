package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Represents an application.  This serves as place to house the
 * application's basic metadata such as app id and display name.
 *
 * Since an application can exist on multiple platforms, this seeks
 * to simply tie all platforms together.
 *
 * Created by patricktwohig on 7/9/15.
 */
@ApiModel
public class Application {

    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WORD_ONLY)
    private String name;

    private String description;

    private String scriptRepoUrl;

    /**
     * The globally-unique identifier.
     *
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * The identifier.
     *
     * @param id the identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the unique name of the application.  Must be alpha-numeric
     * as it is used to form REST URLs.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the application.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.  This is just an optional description for
     * the application used for reference.
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets script repo URL.
     *
     * @return the script repo URL
     */
    public String getScriptRepoUrl() {
        return scriptRepoUrl;
    }

    /**
     * Sets the script repo URL.
     *
     * @param scriptRepoUrl the script repo URL
     */
    public void setScriptRepoUrl(String scriptRepoUrl) {
        this.scriptRepoUrl = scriptRepoUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Application)) return false;

        Application that = (Application) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        return getScriptRepoUrl() != null ? getScriptRepoUrl().equals(that.getScriptRepoUrl()) : that.getScriptRepoUrl() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getScriptRepoUrl() != null ? getScriptRepoUrl().hashCode() : 0);
        return result;
    }

    /**
     * Used as the key for the application attribute where appropriate.  This is equivalent
     * to the FQN of the {@link Application} class.
     */
    public static final String APPLICATION_ATTRIUTE = Application.class.getName();

}
