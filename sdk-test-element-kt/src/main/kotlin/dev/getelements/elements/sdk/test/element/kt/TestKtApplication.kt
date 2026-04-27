package dev.getelements.elements.sdk.test.element.kt

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute
import dev.getelements.elements.sdk.annotation.ElementServiceExport
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation
import jakarta.ws.rs.core.Application
import org.slf4j.LoggerFactory

@ElementServiceImplementation
@ElementServiceExport(Application::class)
class TestKtApplication : Application() {

    companion object {
        private val logger = LoggerFactory.getLogger(TestKtApplication::class.java)

        @JvmField
        @ElementDefaultAttribute(value = "test-kt", description = "HTTP path prefix for this element.")
        val APP_SERVE_PREFIX: String = "dev.getelements.elements.app.serve.prefix"
    }

    init {
        logger.info("TestKtApplication instantiated (kotlin-reflect cold-start may occur here)")
    }

    override fun getClasses(): Set<Class<*>> = setOf(HelloKtEndpoint::class.java)

}
