package dev.getelements.elements.sdk.model.codegen;

import io.swagger.v3.oas.annotations.media.Schema;

public class CodegenRequest {

    @Schema(description = "The URL for the Element OpenApi spec. Usually /app/rest/elementName/openapi.json")
    public String elementSpecUrl;

    @Schema(description = "The target language that you want to generate the code to.")
    public String language;

    @Schema(description = "The package name to set the generated code to. E.g. com.mycompany.mygame.Elements")
    public String packageName;

    @Schema(description = "Any additional options that you want to pass to the generator")
    public String options;
}
