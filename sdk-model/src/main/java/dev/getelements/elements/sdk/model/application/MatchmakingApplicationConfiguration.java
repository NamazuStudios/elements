package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Schema(
    description =
            "This configures the matchmaking system for the application. This defines the queue name and the " +
            "Matchmacker to use when finding players to match. Currently FIFO is builtin and is the default."
)
public class MatchmakingApplicationConfiguration extends ApplicationConfiguration {

    @Schema(description = "Specifies the name of the Matchmaker within the Elements.")
    private String matchmakerName;

    @Schema(description = "Specifies the Element which provides the the Matchmaker.")
    private String matchmakerElement;

    @Schema(description = "The callback definition for when a successful match is made.")
    private CallbackDefinition success;

    public String getMatchmakerName() {
        return matchmakerName;
    }

    public void setMatchmakerName(String matchmakerName) {
        this.matchmakerName = matchmakerName;
    }

    public String getMatchmakerElement() {
        return matchmakerElement;
    }

    public void setMatchmakerElement(String matchmakerElement) {
        this.matchmakerElement = matchmakerElement;
    }

    public CallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(CallbackDefinition success) {
        this.success = success;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MatchmakingApplicationConfiguration that = (MatchmakingApplicationConfiguration) o;
        return Objects.equals(matchmakerName, that.matchmakerName) && Objects.equals(matchmakerElement, that.matchmakerElement) && Objects.equals(success, that.success);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), matchmakerName, matchmakerElement, success);
    }

    @Override
    public String toString() {
        return "MatchmakingApplicationConfiguration{" +
                "matchmakerName='" + matchmakerName + '\'' +
                ", matchmakerElement='" + matchmakerElement + '\'' +
                ", success=" + success +
                '}';
    }

}
