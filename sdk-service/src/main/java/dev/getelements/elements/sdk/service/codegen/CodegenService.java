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
     * @param language The language to generate
     * @param options Any options to pass to the generator
     */
    File generateCore(String language, String options);

    /**
     * Generates the code for a specific application and returns a zip file containing the generated code
     * @param language The language to generate
     * @param options Any options to pass to the generator
     */
    File generateApplication(String applicationNameOrId, String language, String options);
}
