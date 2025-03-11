package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

import static dev.getelements.elements.sdk.model.Constants.Regexp.NO_WHITE_SPACE;

@Schema(description = "Defines a script method and a module to invoke.")
public class CallbackDefinition implements Serializable {

    @NotNull
    @Pattern(regexp = NO_WHITE_SPACE)
    @Schema(description = "Specifies the module to invoke.")
    private String module;

    @NotNull
    @Pattern(regexp = NO_WHITE_SPACE)
    @Schema(description = "Specifies the method to invoke.")
    private String method;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
