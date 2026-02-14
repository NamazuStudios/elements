@ElementService(ElementRuntimeService.class)
@ElementService(ElementContainerService.class)
@ElementDefinition(
        value = "dev.getelements.elements.deployment",
        additionalPackages = @ElementPackage(value = "dev.getelements.elements.sdk.deployment", recursive = true)
)
package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementPackage;
import dev.getelements.elements.sdk.annotation.ElementService;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
