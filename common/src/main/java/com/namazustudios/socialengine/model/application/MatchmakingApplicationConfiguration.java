package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static com.namazustudios.socialengine.Constants.Regexp.WORD_ONLY;

@ApiModel(
    value = "Matchmaking Applicaiont Configuration",
    description = "This configures the matchmaking system.  More specifically, this configures which scripts and" +
                  "methods will be called when a successful match has been made.")
public class MatchmakingApplicationConfiguration extends ApplicationConfiguration {

    @NotNull
    @Pattern(regexp = WORD_ONLY)
    @ApiModelProperty("A user-sepecified unqiue identifier for the matching scheme.  It is possible to specify " +
                      "multiple schemes per application, but each must be uniquely named.  Each scheme allows for the " +
                      "specification of different scripts to handle the successful match.  When requesting matchmaking " +
                      "services clients will specify the scheme to be used.")
    private String scheme;

    @NotNull
    @ApiModelProperty("Specifies the matching algorithm to use.  Algorithms are builtin and implemented by the API " +
                      "services.  Currently, only FIFO is supported.")
    private MatchingAlgorithm algorithm;

    @NotNull
    @ApiModelProperty("Specifies the callback to execute when a successful match has been made.  When invoked, the " +
                      "method will receive Match object generated as the result of the matchmaking process.  Match " +
                      "instances will easily ")
    private CallbackDefinition success;

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

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
