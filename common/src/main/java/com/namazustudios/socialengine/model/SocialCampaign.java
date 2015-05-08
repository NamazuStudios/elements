package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
public class SocialCampaign {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
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
