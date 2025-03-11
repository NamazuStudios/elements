
@ElementDefinition(recursive = true)
@ElementService(
        value = TestService.class,
        implementation = @ElementServiceImplementation(TestServiceImplementation.class)
)
package dev.getelements.elements.sdk.test.element.a;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementService;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import dev.getelements.elements.sdk.test.element.TestService;
