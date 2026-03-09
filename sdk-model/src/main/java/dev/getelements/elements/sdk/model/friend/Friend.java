package dev.getelements.elements.sdk.model.friend;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Represents a player's friend.
 */
@Schema(description =
        "Represents a player's friend.  This includes the basic information of the friend as well as " +
        "the friendship type, profiles he or she has across games, and "
)
public class Friend {

    /** Creates a new instance. */
    public Friend() {}

    @Schema(description = "The unique ID of the friendship.")
    private String id;

    @Schema(description = "The user assocaited with this particular friend.")
    private User user;

    @Schema(description = "The friendship type.")
    private Friendship friendship;

    @Schema(description = "The profiles which are associated with the friend user.")
    private List<Profile> profiles;

    /**
     * Returns the unique ID of the friendship.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the friendship.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the user associated with this friend.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this friend.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the friendship type.
     *
     * @return the friendship
     */
    public Friendship getFriendship() {
        return friendship;
    }

    /**
     * Sets the friendship type.
     *
     * @param friendship the friendship
     */
    public void setFriendship(Friendship friendship) {
        this.friendship = friendship;
    }

    /**
     * Returns the profiles associated with the friend user.
     *
     * @return the profiles
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Sets the profiles associated with the friend user.
     *
     * @param profiles the profiles
     */
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;

        Friend friend = (Friend) o;

        if (getId() != null ? !getId().equals(friend.getId()) : friend.getId() != null) return false;
        if (getUser() != null ? !getUser().equals(friend.getUser()) : friend.getUser() != null) return false;
        if (getFriendship() != friend.getFriendship()) return false;
        return getProfiles() != null ? getProfiles().equals(friend.getProfiles()) : friend.getProfiles() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getFriendship() != null ? getFriendship().hashCode() : 0);
        result = 31 * result + (getProfiles() != null ? getProfiles().hashCode() : 0);
        return result;
    }

}
