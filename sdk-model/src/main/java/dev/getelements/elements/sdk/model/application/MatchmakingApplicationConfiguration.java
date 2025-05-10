package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.Objects;

@Schema(
    description =
            "This configures the matchmaking system for the application. This defines the queue name and the " +
            "Matchmacker to use when finding players to match. Currently FIFO is builtin and is the default."
)
public class MatchmakingApplicationConfiguration extends ApplicationConfiguration {

    @Valid
    @Schema(description = "The callback definition for when a successful match is made.")
    private CallbackDefinition success;

    @Valid
    @Schema(description = "Describes the matchmaker (dev.getelements.elements.sdk.dao.Matchmaker) to use for this configuration.")
    private ElementServiceReference matchmaker;

    public CallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(CallbackDefinition success) {
        this.success = success;
    }

    public ElementServiceReference getMatchmaker() {
        return matchmaker;
    }

    public void setMatchmaker(ElementServiceReference matchmaker) {
        this.matchmaker = matchmaker;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        MatchmakingApplicationConfiguration that = (MatchmakingApplicationConfiguration) object;
        return Objects.equals(getSuccess(), that.getSuccess()) && Objects.equals(getMatchmaker(), that.getMatchmaker());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSuccess(), getMatchmaker());
    }

}
