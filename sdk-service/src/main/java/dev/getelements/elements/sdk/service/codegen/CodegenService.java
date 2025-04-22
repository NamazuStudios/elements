package dev.getelements.elements.sdk.service.codegen;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.io.File;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Interface for code generation services
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface CodegenService {

    /**
     * Generates the Elements Core APIs and returns a zip file containing the generated code
     * @param spec The file containing the generated OpenAPI spec
     * @param language The language to generate
     * @param options Any options to pass to the generator
     * @return The file that will contain the compressed contents after code generation
     */
    File generateCore(File spec, String language, String packageName, String options);

    /**
     * Generates the code for a specific application and returns a zip file containing the generated code
     * @param spec The file containing the generated OpenAPI spec
     * @param applicationNameOrId The name or id of the application to generate code for
     * @param language The language to generate
     * @param options Any options to pass to the generator
     * @return The file that will contain the compressed contents after code generation
     */
    File generateApplication(File spec, String applicationNameOrId, String language, String packageName, String options);
}
