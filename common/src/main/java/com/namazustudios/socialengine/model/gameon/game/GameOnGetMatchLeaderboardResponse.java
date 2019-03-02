package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description = "Corresponds to the GetMatchLeaderboardResponse: " +
                        "https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchleaderboardresponse")
public class GameOnGetMatchLeaderboardResponse {

    @ApiModelProperty("The current player's rank.")
    private LeaderboardItem currentPlayer;

    @ApiModelProperty("List of leaderboard items.")
    private List<LeaderboardItem> leaderboard;

    @ApiModelProperty("The neighboring leaderboard items.")
    private List<LeaderboardItem> neighbors;

    @ApiModelProperty("The cursor to fetch the next page of results. If this is null, then there are no more results" +
            " to fetch. This value is dynamically computed to retrieve the cursor provided in the `next` property.")
    private String cursor;

    @ApiModelProperty("The raw next string retrieved from the GameOn service. Use the `cursor` property for " +
            "pagination instead.")
    private String next;

    public LeaderboardItem getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(LeaderboardItem currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public List<LeaderboardItem> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<LeaderboardItem> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public List<LeaderboardItem> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<LeaderboardItem> neighbors) {
        this.neighbors = neighbors;
    }

    public String getCursor() {
        if (getNext() == null) {
            return null;
        }

        // for now, we assume the next string is of the form:
        // `/matches/f106bf39-adc2-4a19-8934-fdaf482738e6/leaderboard?limit=2&cursor=QHxmZWQy...`

        final int questionMarkIndex = getNext().indexOf("?");
        if (questionMarkIndex < 0) {
            return null;
        }

        final String queryString = getNext().substring(questionMarkIndex + 1);
        if (queryString == null || queryString.length() == 0) {
            return null;
        }

        final String[] queryPairs = queryString.split("&");
        if (queryPairs == null || queryPairs.length == 0) {
            return null;
        }

        for (final String queryPair : queryPairs) {
            if (queryPair.startsWith("cursor")) {
                final int equalSignIndex = queryPair.indexOf("=");
                if (equalSignIndex < 0) {
                    return null;
                }

                final String cursorValue = queryPair.substring(equalSignIndex + 1);
                return cursorValue;
            }
        }

        return null;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnGetMatchLeaderboardResponse)) return false;
        GameOnGetMatchLeaderboardResponse that = (GameOnGetMatchLeaderboardResponse) object;
        return Objects.equals(getCurrentPlayer(), that.getCurrentPlayer()) &&
                Objects.equals(getLeaderboard(), that.getLeaderboard()) &&
                Objects.equals(getNeighbors(), that.getNeighbors());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrentPlayer(), getLeaderboard(), getNeighbors());
    }

    @Override
    public String toString() {
        return "GameOnGetMatchLeaderboardResponse{" +
                "currentPlayer=" + currentPlayer +
                ", leaderboard=" + leaderboard +
                ", neighbors=" + neighbors +
                '}';
    }

    @ApiModel(description = "Corresponds to the GetMatchLeaderboardResponse_LeaderboardItem:" +
                            "https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchleaderboardresponse_leaderboarditem")
    public static class LeaderboardItem {

        private Profile profile;

        private String externalPlayerId;

        private Boolean isCurrentPlayer;

        private String playerName;

        private Integer rank;

        private Long score;

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        public String getExternalPlayerId() {
            return externalPlayerId;
        }

        public void setExternalPlayerId(String externalPlayerId) {
            this.externalPlayerId = externalPlayerId;
        }

        public Boolean getIsCurrentPlayer() {
            return isCurrentPlayer;
        }

        public void setIsCurrentPlayer(Boolean isCurrentPlayer) {
            isCurrentPlayer = isCurrentPlayer;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }

        public Long getScore() {
            return score;
        }

        public void setScore(Long score) {
            this.score = score;
        }



        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof LeaderboardItem)) return false;
            LeaderboardItem that = (LeaderboardItem) object;
            return Objects.equals(getProfile(), that.getProfile()) &&
                    Objects.equals(getExternalPlayerId(), that.getExternalPlayerId()) &&
                    Objects.equals(getIsCurrentPlayer(), that.getIsCurrentPlayer()) &&
                    Objects.equals(getPlayerName(), that.getPlayerName()) &&
                    Objects.equals(getRank(), that.getRank()) &&
                    Objects.equals(getScore(), that.getScore());
        }

        @Override
        public int hashCode() {

            return Objects.hash(getProfile(), getExternalPlayerId(), isCurrentPlayer, getPlayerName(), getRank(), getScore());
        }

        @Override
        public String toString() {
            return "LeaderboardItem{" +
                    "profile=" + profile +
                    ", externalPlayerId='" + externalPlayerId + '\'' +
                    ", isCurrentPlayer=" + isCurrentPlayer +
                    ", playerName='" + playerName + '\'' +
                    ", rank=" + rank +
                    ", score=" + score +
                    '}';
        }

    }

}
