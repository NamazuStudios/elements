@ElementPublic
@ElementDefinition(recursive = true)
@ElementDependency("dev.getelements.elements.sdk.mongo")
@ElementService(Datastore.class)
package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDependency;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementService;
import dev.morphia.Datastore;
