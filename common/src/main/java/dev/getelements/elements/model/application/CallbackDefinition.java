package dev.getelements.elements.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

import static dev.getelements.elements.Constants.Regexp.NO_WHITE_SPACE;

@ApiModel(description = "Defines a script method and a module to invoke.")
public class CallbackDefinition implements Serializable {

    @NotNull
    @Pattern(regexp = NO_WHITE_SPACE)
    @ApiModelProperty("Specifies the module to invoke.")
    private String module;

    @NotNull
    @Pattern(regexp = NO_WHITE_SPACE)
    @ApiModelProperty("Specifies the method to invoke.")
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
