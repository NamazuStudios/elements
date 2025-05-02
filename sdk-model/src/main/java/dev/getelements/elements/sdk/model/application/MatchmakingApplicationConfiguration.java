package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.match.MatchingAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
    description = "This configures the matchmaking system.  More specifically, this configures which scripts and" +
                  "methods will be called when a successful match has been made.")
public class MatchmakingApplicationConfiguration extends ApplicationConfiguration {

    @NotNull
    @Schema(description =
            "Specifies the matching algorithm to use.  Algorithms are builtin and implemented by the API " +
            "services.  Currently, only FIFO is supported.")
    private MatchingAlgorithm algorithm;

    @NotNull
    @Schema(description =
            "Specifies the callback to execute when a successful match has been made.  When invoked, the " +
            "method will receive Match object generated as the result of the matchmaking process.  Match " +
            "instances will easily ")
    private CallbackDefinition success;

    public MatchingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(MatchingAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public CallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(CallbackDefinition success) {
        this.success = success;
    }

}
