package ${package}

import ${package}.rest.ExampleContent
import ${package}.rest.HelloWithAuthentication
import ${package}.rest.HelloWorld
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute
import dev.getelements.elements.sdk.annotation.ElementServiceExport
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation
import jakarta.ws.rs.core.Application

// @ElementServiceImplementation registers this class as the JAX-RS Application for this Element.
// @ElementServiceExport is required for Jersey to discover the application, but expose = false
// prevents the binding from being pushed into the shared parent injector. Without expose = false,
// every deployed Element would try to export its own Application binding to the same parent
// injector, causing a BindingAlreadySet error at startup when more than one Element is loaded.
@ElementServiceImplementation
@ElementServiceExport(value = [Application::class], expose = false)
class HelloWorldApplication : Application() {

    companion object {

        @JvmField
        @ElementDefaultAttribute("true")
        val AUTH_ENABLED: String = "dev.getelements.elements.auth.enabled"

        @JvmField
        @ElementDefaultAttribute("example-element")
        val APPLICATION_PREFIX: String = "dev.getelements.elements.app.serve.prefix"

        @JvmField
        @ElementDefaultAttribute("/app/static/test/path")
        val STATIC_CONTENT_URI: String = "dev.getelements.element.static.uri"

        @JvmField
        @ElementDefaultAttribute("/app/ui/test/path")
        val UI_CONTENT_URI: String = "dev.getelements.element.ui.uri"

        const val OPENAPI_TAG: String = "Example"
    }

    /**
     * Here we register all the classes that we want to be included in the Element.
     */
    override fun getClasses(): Set<Class<*>> = setOf(
        // Endpoints
        HelloWorld::class.java,
        HelloWithAuthentication::class.java,
        ExampleContent::class.java,

        // Exposes the default security rules for the API. Assumes you are using the builtin Elements auth
        // system by setting `dev.getelements.elements.auth.enabled` to true in the annotation above.
        OpenAPISecurityConfig::class.java
    )

}