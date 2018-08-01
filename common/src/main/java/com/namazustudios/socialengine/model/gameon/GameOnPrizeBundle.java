package com.namazustudios.socialengine.model.gameon;

import io.swagger.annotations.ApiModel;

import java.util.List;
import java.util.Objects;

@ApiModel
public class GameOnPrizeBundle {

    private String title;

    private String description;

    private String imageUrl;

    private List<String> prizeIds;

    private Integer rankFrom;

    private Integer rankTo;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<String> getPrizeIds() {
        return prizeIds;
    }

    public void setPrizeIds(List<String> prizeIds) {
        this.prizeIds = prizeIds;
    }

    public Integer getRankFrom() {
        return rankFrom;
    }

    public void setRankFrom(Integer rankFrom) {
        this.rankFrom = rankFrom;
    }

    public Integer getRankTo() {
        return rankTo;
    }

    public void setRankTo(Integer rankTo) {
        this.rankTo = rankTo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnPrizeBundle)) return false;
        GameOnPrizeBundle that = (GameOnPrizeBundle) object;
        return Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getImageUrl(), that.getImageUrl()) &&
                Objects.equals(getPrizeIds(), that.getPrizeIds()) &&
                Objects.equals(getRankFrom(), that.getRankFrom()) &&
                Objects.equals(getRankTo(), that.getRankTo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDescription(), getImageUrl(), getPrizeIds(), getRankFrom(), getRankTo());
    }

    @Override
    public String toString() {
        return "GameOnPrizeBundle{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", prizeIds=" + prizeIds +
                ", rankFrom=" + rankFrom +
                ", rankTo=" + rankTo +
                '}';
    }

}
