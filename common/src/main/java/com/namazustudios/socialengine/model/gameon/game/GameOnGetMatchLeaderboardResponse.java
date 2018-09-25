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

    @ApiModelProperty("The neighboring leadeerboard items.")
    private List<LeaderboardItem> neighbors;

    @ApiModelProperty("The URL for the next page of results.")
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
                Objects.equals(getNeighbors(), that.getNeighbors()) &&
                Objects.equals(getNext(), that.getNext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrentPlayer(), getLeaderboard(), getNeighbors(), getNext());
    }

    @Override
    public String toString() {
        return "GetMatchLeaderboardResponse{" +
                "currentPlayer=" + currentPlayer +
                ", leaderboard=" + leaderboard +
                ", neighbors=" + neighbors +
                ", next='" + next + '\'' +
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

        public Boolean getCurrentPlayer() {
            return isCurrentPlayer;
        }

        public void setCurrentPlayer(Boolean currentPlayer) {
            isCurrentPlayer = currentPlayer;
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
                    Objects.equals(getCurrentPlayer(), that.getCurrentPlayer()) &&
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
