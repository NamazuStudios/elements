// Required annotation for an Element. Will recursively search folders
// from this point to include classes in the Element if recursive is true.
// Otherwise, you must include additional package-info.java files in child packages.
@ElementDefinition(recursive = true)
// Enables DI via Guice, eagerly constructing TestRedeployIndexInitializer at Element-load time.
@GuiceElementModule(TestRedeployGuiceModule.class)
// Allows injecting the shared Morphia Datastore from the platform's sdk-mongo Element.
@ElementDependency("dev.getelements.elements.sdk.mongo")
package dev.getelements.elements.sdk.test.element.mongo;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDependency;
import dev.getelements.elements.sdk.spi.guice.annotations.GuiceElementModule;
