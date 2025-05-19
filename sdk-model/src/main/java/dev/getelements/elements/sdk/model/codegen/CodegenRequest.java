package dev.getelements.elements.sdk.model.codegen;

import io.swagger.v3.oas.annotations.media.Schema;

public class CodegenRequest {

    @Schema(description = "The application name or id that you want to generate API code for. " +
            "If left null or empty, the Elements core API will be generated instead.")
    public String applicationNameOrId;

    @Schema(description = "The target language that you want to generate the code to.")
    public String language;

    @Schema(description = "The package name to set the generated code to. E.g. com.mycompany.mygame.Elements")
    public String packageName;

    @Schema(description = "Any additional options that you want to pass to the generator")
    public String options;
}
