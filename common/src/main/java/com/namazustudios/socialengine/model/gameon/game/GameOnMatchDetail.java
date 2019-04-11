package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.match.Match;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApiModel(description = "Represents a GameOn Match.  Maps direclty to the Amazon GameOn APIs.  Contains slightly more" +
        "information than its summary counterpart.")
public class GameOnMatchDetail {

    @ApiModelProperty("The GameOn assigned match ID.")
    private String matchId;

    @ApiModelProperty("The details of the associated tournament.")
    private GameOnTournamentDetail tournamentDetails;

    @ApiModelProperty("Prizes awarded to the player.")
    private List<AwardedPrize> awardedPrizes;

    @ApiModelProperty("True if the player can enter, false otherwise.")
    private Boolean canEnter;

    @ApiModelProperty("The last score recorded for the match.")
    private Long lastScore;

    @ApiModelProperty("The date at which the last score was made.")
    private Long lastScoreDate;

    @ApiModelProperty("The player's overall score for the match.")
    private Long score;

    @ApiModelProperty("The date the score was submitted.")
    private Long scoreDate;

    @ApiModelProperty("Remaining number of attempts if the player is already in the match.")
    private Integer attemptsRemaining;

    @ApiModelProperty("The metadata of the tournament - text for any additional information (e.g. Rules, Disclaimers, etc.)")
    private String metadata;

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public GameOnTournamentDetail getTournamentDetails() {
        return tournamentDetails;
    }

    public void setTournamentDetails(GameOnTournamentDetail tournamentDetails) {
        this.tournamentDetails = tournamentDetails;
    }

    public List<AwardedPrize> getAwardedPrizes() {
        return awardedPrizes;
    }

    public void setAwardedPrizes(List<AwardedPrize> awardedPrizes) {
        this.awardedPrizes = awardedPrizes;
    }

    public Boolean getCanEnter() {
        return canEnter;
    }

    public void setCanEnter(Boolean canEnter) {
        this.canEnter = canEnter;
    }

    public Long getLastScore() {
        return lastScore;
    }

    public void setLastScore(Long lastScore) {
        this.lastScore = lastScore;
    }

    public Long getLastScoreDate() {
        return lastScoreDate;
    }

    public void setLastScoreDate(Long lastScoreDate) {
        this.lastScoreDate = lastScoreDate;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Long getScoreDate() {
        return scoreDate;
    }

    public void setScoreDate(Long scoreDate) {
        this.scoreDate = scoreDate;
    }

    public Integer getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public void setAttemptsRemaining(Integer attemptsRemaining) {
        this.attemptsRemaining = attemptsRemaining;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnMatchDetail)) return false;
        GameOnMatchDetail that = (GameOnMatchDetail) object;
        return Objects.equals(getMatchId(), that.getMatchId()) &&
                Objects.equals(getTournamentDetails(), that.getTournamentDetails()) &&
                Objects.equals(getAwardedPrizes(), that.getAwardedPrizes()) &&
                Objects.equals(getCanEnter(), that.getCanEnter()) &&
                Objects.equals(getLastScore(), that.getLastScore()) &&
                Objects.equals(getLastScoreDate(), that.getLastScoreDate()) &&
                Objects.equals(getScore(), that.getScore()) &&
                Objects.equals(getScoreDate(), that.getScoreDate()) &&
                Objects.equals(getAttemptsRemaining(), that.getAttemptsRemaining()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getMatchId(), getTournamentDetails(), getAwardedPrizes(), getCanEnter(), getLastScore(), getLastScoreDate(), getScore(), getScoreDate(), getAttemptsRemaining(), getMetadata());
    }

    @Override
    public String toString() {
        return "GameOnMatchDetail{" +
                "matchId='" + matchId + '\'' +
                ", tournamentDetails=" + tournamentDetails +
                ", awardedPrizes=" + awardedPrizes +
                ", canEnter=" + canEnter +
                ", lastScore=" + lastScore +
                ", lastScoreDate=" + lastScoreDate +
                ", score=" + score +
                ", scoreDate=" + scoreDate +
                ", attemptsRemaining=" + attemptsRemaining +
                ", metadata='" + metadata + '\'' +
                '}';
    }

    @ApiModel(description = "The awarded prize model.  See: " +
            "https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchdetailsresponse_awardedprize")
    public static class AwardedPrize {

        @ApiModelProperty("The awarded prize ID")
        private String awardedPrizeId;

        @ApiModelProperty("The awarded prize status.")
        private AwardedPrizeStatus status;

        @ApiModelProperty("The awarded prize title.")
        private String prizeTitle;

        @ApiModelProperty("Date prize expires.")
        private Long dateOfExpiration;

        @ApiModelProperty("Prize description.")
        private String description;

        @ApiModelProperty("Prize icon.")
        private String imageUrl;

        @ApiModelProperty("Describes what is contained in prizeInfo (VENDOR | AMAZON_PHYSICAL).")
        private String prizeInfoType;

        public String getAwardedPrizeId() {
            return awardedPrizeId;
        }

        public void setAwardedPrizeId(String awardedPrizeId) {
            this.awardedPrizeId = awardedPrizeId;
        }

        public AwardedPrizeStatus getStatus() {
            return status;
        }

        public void setStatus(AwardedPrizeStatus status) {
            this.status = status;
        }

        public String getPrizeTitle() {
            return prizeTitle;
        }

        public void setPrizeTitle(String prizeTitle) {
            this.prizeTitle = prizeTitle;
        }

        public Long getDateOfExpiration() {
            return dateOfExpiration;
        }

        public void setDateOfExpiration(Long dateOfExpiration) {
            this.dateOfExpiration = dateOfExpiration;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPrizeInfoType() {
            return prizeInfoType;
        }

        public void setPrizeInfoType(String prizeInfoType) {
            this.prizeInfoType = prizeInfoType;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof AwardedPrize)) return false;
            AwardedPrize that = (AwardedPrize) object;
            return Objects.equals(getAwardedPrizeId(), that.getAwardedPrizeId()) &&
                    getStatus() == that.getStatus() &&
                    Objects.equals(getPrizeTitle(), that.getPrizeTitle()) &&
                    Objects.equals(getDateOfExpiration(), that.getDateOfExpiration()) &&
                    Objects.equals(getDescription(), that.getDescription()) &&
                    Objects.equals(getImageUrl(), that.getImageUrl()) &&
                    Objects.equals(getPrizeInfoType(), that.getPrizeInfoType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAwardedPrizeId(), getStatus(), getPrizeTitle(), getDateOfExpiration(), getDescription(), getImageUrl(), getDateOfExpiration());
        }

        @Override
        public String toString() {
            return "AwardedPrize{" +
                    "awardedPrizeId='" + awardedPrizeId + '\'' +
                    ", status=" + status +
                    ", prizeTitle='" + prizeTitle + '\'' +
                    ", prizeTitle='" + dateOfExpiration + '\'' +
                    ", prizeTitle='" + description + '\'' +
                    ", prizeTitle='" + imageUrl + '\'' +
                    ", prizeTitle='" + prizeInfoType + '\'' +
                    '}';
        }

    }

    @ApiModel(description = "The awarded prize status. See: " +
            "https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchlistresponse_match")
    public enum AwardedPrizeStatus {

        /**
         * The prize has yet to be claimed.
         */
        UNCLAIMED,

        /**
         * The prize has been claimed.
         */
        CLAIMED,

        /**
         * The prize has been fulfilled.
         */
        FULFILLED

    }
}
