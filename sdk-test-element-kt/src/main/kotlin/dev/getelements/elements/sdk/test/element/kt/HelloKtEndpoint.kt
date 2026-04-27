package dev.getelements.elements.sdk.test.element.kt

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.slf4j.LoggerFactory

@Path("/hello")
class HelloKtEndpoint {

    companion object {
        private val logger = LoggerFactory.getLogger(HelloKtEndpoint::class.java)
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello(): String {
        logger.debug("HelloKtEndpoint.hello() invoked")
        return "Hello from Kotlin!"
    }

}
