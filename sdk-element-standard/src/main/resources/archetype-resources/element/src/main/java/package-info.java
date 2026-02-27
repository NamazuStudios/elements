#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
// Required annotation for an Element. Will recursively search folders
// from this point to include classes in the Element if recursive is true.
// Otherwise, you must include additional package-info.java files in child packages.
@ElementDefinition(recursive = true)
// Enables DI via Guice
@GuiceElementModule(MyGameModule.class)
// Allows injecting DAO layer from Elements Core
@ElementDependency("dev.get${artifactId}s.${artifactId}s.sdk.dao")
// Allows injecting Service layer from Elements Core
@ElementDependency("dev.get${artifactId}s.${artifactId}s.sdk.service")
package ${package};

import ${package}.guice.MyGameModule;
import dev.get${artifactId}s.${artifactId}s.sdk.annotation.ElementDefinition;
import dev.get${artifactId}s.${artifactId}s.sdk.annotation.ElementDependency;
import dev.get${artifactId}s.${artifactId}s.sdk.spi.guice.annotations.GuiceElementModule;