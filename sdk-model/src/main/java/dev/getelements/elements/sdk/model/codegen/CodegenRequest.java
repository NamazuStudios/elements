package dev.getelements.elements.sdk.model.codegen;

import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a request to generate client code from an Element OpenAPI spec. */
public class CodegenRequest {

    /** Creates a new instance. */
    public CodegenRequest() {}

    /** The URL for the Element OpenApi spec. Usually /app/rest/elementName/openapi.json. */
    @Schema(description = "The URL for the Element OpenApi spec. Usually /app/rest/elementName/openapi.json")
    public String elementSpecUrl;

    /** The target language to generate code for. */
    @Schema(description = "The target language that you want to generate the code to.")
    public String language;

    /** The package name for the generated code. */
    @Schema(description = "The package name to set the generated code to. E.g. com.mycompany.mygame.Elements")
    public String packageName;

    /** Any additional options to pass to the generator. */
    @Schema(description = "Any additional options that you want to pass to the generator")
    public String options;
}
