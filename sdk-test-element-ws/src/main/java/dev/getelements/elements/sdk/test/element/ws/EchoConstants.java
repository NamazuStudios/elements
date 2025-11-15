package dev.getelements.elements.sdk.test.element.ws;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;

public interface EchoConstants {

    @ElementDefaultAttribute(value = "myapp", description = "The application name used in the echo service")
    String APP_SERVE_PREFIX = "dev.getelements.elements.app.serve.prefix";

}
