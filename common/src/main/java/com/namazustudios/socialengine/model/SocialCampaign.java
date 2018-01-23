package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Represents a particular social campaign within the system.  A socialengine is basically a campaign to collect names
 * while simultaneously requiring users share their entries with others via various social networking means.
 *
 * A player is only successfully entered when the server can verify that the link was shared on a social network,
 * such as Facebook.
 *
 * Created by patricktwohig on 3/18/15.
 *
 */
@ApiModel
public class SocialCampaign implements Serializable {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String linkUrl;

    @NotNull
    @Size(min = 1)
    private List<EntrantType> allowedEntrantTypes;

    private Date beginDate;

    private Date endDate;

    /**
     * An all-lowercase, no-spaces string which is used to identify the
     * campaign internally.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This is the link URL which is to be shared by the user.
     *
     * @return the link URL
     */
    public String getLinkUrl() {
        return linkUrl;
    }

    /**
     * Sets the link URL which is to be shared by the user.
     *
     * @param linkUrl
     */
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    /**
     * A list of allowable entrant types.  Practically speaking, this must have at least
     * a single entry.  Since separate data is required from each type of entrant, this
     * allows us to fine-tune what types we're collecting.
     *
     * @return the entrant types
     */
    public List<EntrantType> getAllowedEntrantTypes() {
        return allowedEntrantTypes;
    }

    /**
     * Sets the allowed entrant types.
     *
     * @param allowedEntrantTypes
     */
    public void setAllowedEntrantTypes(List<EntrantType> allowedEntrantTypes) {
        this.allowedEntrantTypes = allowedEntrantTypes;
    }

    /**
     * Gets the socialengine's beginning date.
     *
     * @return the socialengine's beginning date
     */
    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * SEts the promotion's end date.
     * @param beginDate
     */
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    /**
     * Gets the promotion's end date.
     * @return
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * SEts the socialengine's end date.
     * @param endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocialCampaign)) return false;

        SocialCampaign that = (SocialCampaign) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getLinkUrl() != null ? !getLinkUrl().equals(that.getLinkUrl()) : that.getLinkUrl() != null) return false;
        if (getAllowedEntrantTypes() != null ? !getAllowedEntrantTypes().equals(that.getAllowedEntrantTypes()) : that.getAllowedEntrantTypes() != null)
            return false;
        if (getBeginDate() != null ? !getBeginDate().equals(that.getBeginDate()) : that.getBeginDate() != null)
            return false;
        return getEndDate() != null ? getEndDate().equals(that.getEndDate()) : that.getEndDate() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getLinkUrl() != null ? getLinkUrl().hashCode() : 0);
        result = 31 * result + (getAllowedEntrantTypes() != null ? getAllowedEntrantTypes().hashCode() : 0);
        result = 31 * result + (getBeginDate() != null ? getBeginDate().hashCode() : 0);
        result = 31 * result + (getEndDate() != null ? getEndDate().hashCode() : 0);
        return result;
    }

    /**
     * A list of entrant allowedEntrantTypes for the campaign.
     */
    public enum EntrantType {

        /**
         * Enables basic socialengineal entrant.
         */
        BASIC,

        /**
         * Enables Steam specific socialengineal entrant
         */
        STEAM

    }

}
