package com.namazustudios.socialengine.model.match;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by patricktwohig on 7/18/17.
 */
@ApiModel(description =
        "Represents a single one-on-one match between the current player and an opponent.  Once " +
        "matched, the player will will be able to create a game against the supplied opposing player.  The " +
        "server may modify or delete matches based on a variety of circumstances.")
public class Match implements Serializable {

    public static final String ROOT_TOPIC = "match";

    @Null(groups = Create.class)
    @ApiModelProperty("The unique ID of the match.")
    private String id;

    @NotNull(groups = {Create.class, Insert.class})
    @ApiModelProperty("The scheme to use when matching with other players.")
    private String scheme;

    @ApiModelProperty("The player requesting the match.  If not specified, then the current profile will be inferred.")
    private Profile player;

    @Null(groups = {Create.class, Insert.class})
    @ApiModelProperty("The opposing player, or null if no suitable opponent has been found.")
    private Profile opponent;

    @Null
    @ApiModelProperty("The time of the last modification of the match.")
    private long lastUpdatedTimestamp;

    @Null
    @ApiModelProperty("The system-assigned game ID of the match.  Null until the match is successfully made.")
    private String gameId;

    /**
     * Gets the unique server-assigned ID of this match.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique server-assigned ID of this match.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the desired matchmaking scheme.
     *
     * @return the desired matchmaking scheme.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the desired matchmaking scheme.
     * @param scheme
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the {@link Profile} of the player requesting the match.
     *
     * @return the player's {@link Profile}
     */
    public Profile getPlayer() {
        return player;
    }

    /**
     * Sets the {@link Profile} of the player requesting the match.
     *
     * @param player the player
     */
    public void setPlayer(Profile player) {
        this.player = player;
    }

    /**
     * The {@link Profile} belonging to the opponent.
     *
     * @return the opponent
     */
    public Profile getOpponent() {
        return opponent;
    }

    /**
     * Sets the {@link Profile} belonging to the opponent.
     *
     * @param opponent the opponent
     */
    public void setOpponent(Profile opponent) {
        this.opponent = opponent;
    }

    /**
     * Gets the date at which the last modification was made to this match.
     *
     * @return the last-updated date
     */
    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    /**
     * Sets the date at which the last modification was made to this match.
     *
     * @param lastUpdatedTimestamp the last-updated date
     */
    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    /**
     * Gets the system assigned game ID for the match.
     *
     * @return the system-assigned game id
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the system assigned game ID for the match.
     * @param gameId
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Match)) return false;
        Match match = (Match) object;
        return getLastUpdatedTimestamp() == match.getLastUpdatedTimestamp() &&
                Objects.equals(getId(), match.getId()) &&
                Objects.equals(getScheme(), match.getScheme()) &&
                Objects.equals(getPlayer(), match.getPlayer()) &&
                Objects.equals(getOpponent(), match.getOpponent()) &&
                Objects.equals(getGameId(), match.getGameId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getScheme(), getPlayer(), getOpponent(), getLastUpdatedTimestamp(), getGameId());
    }

    @Override
    public String toString() {
        return "Match{" +
                "id='" + id + '\'' +
                ", scheme='" + scheme + '\'' +
                ", player=" + player +
                ", opponent=" + opponent +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", gameId='" + gameId + '\'' +
                '}';
    }

}
