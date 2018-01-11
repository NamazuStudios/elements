package com.namazustudios.socialengine.model.match;

import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Null;
import java.io.Serializable;

/**
 * Created by patricktwohig on 7/18/17.
 */
@ApiModel(description =
        "Represents a single one-on-one match between the current player and an opponent.  Once " +
        "matched, the player will will be able to create a game against the supplied opposing player.  The " +
        "server may modify or delete matches based on a variety of circumstances.")
public class Match implements Serializable {

    @ApiModelProperty("The unique ID of the match.")
    private String id;

    @ApiModelProperty("The player requesting the match.  If not specified, then the current profile will be inferred.")
    private Profile player;

    @Null
    @ApiModelProperty("The opposing player, or null if no suitable opponent has been found.")
    private Profile opponent;

    @Null
    @ApiModelProperty("The ID of the game currently being played against the opponent.  null if the game " +
                       "has not been initiated yet.")
    private String gameId;

    @ApiModelProperty("The time of the last modification of the match.")
    private long lastUpdatedTimestamp;

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
     * Gets the ID of the game associated with this match.
     *
     * @return the game ID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the game ID associated with this match.
     * @param gameId
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match)) return false;

        Match match = (Match) o;

        if (getLastUpdatedTimestamp() != match.getLastUpdatedTimestamp()) return false;
        if (getId() != null ? !getId().equals(match.getId()) : match.getId() != null) return false;
        if (getOpponent() != null ? !getOpponent().equals(match.getOpponent()) : match.getOpponent() != null)
            return false;
        return getGameId() != null ? getGameId().equals(match.getGameId()) : match.getGameId() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getOpponent() != null ? getOpponent().hashCode() : 0);
        result = 31 * result + (getGameId() != null ? getGameId().hashCode() : 0);
        result = 31 * result + (int) (getLastUpdatedTimestamp() ^ (getLastUpdatedTimestamp() >>> 32));
        return result;
    }

}
