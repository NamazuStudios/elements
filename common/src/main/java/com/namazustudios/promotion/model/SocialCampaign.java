package com.namazustudios.promotion.model;

import java.util.List;

/**
 * Represents a particular social campaign within the system.  A promotion is basically a campaign to collect names
 * while simultaneously requiring users share their entries with others via various social networking means.
 *
 * A player is only successfully entered when the server can verify that the link was shared on a social network,
 * such as Facebook.
 *
 * Created by patricktwohig on 3/18/15.
 *
 */
public class SocialCampaign {

    private String name;

    private String linkUrl;

    private List<EntrantType> allowedEntrantTypes;

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

    public void setAllowedEntrantTypes(List<EntrantType> allowedEntrantTypes) {
        this.allowedEntrantTypes = allowedEntrantTypes;
    }

    /**
     * A list of entrant allowedEntrantTypes for the campaign.
     */
    public enum EntrantType {

        /**
         * Enables basic promotional entrant.
         */
        BASIC,

        /**
         * Enables Steam specific promotional entrant
         */
        STEAM

    }

}
