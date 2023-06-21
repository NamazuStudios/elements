package dev.getelements.elements.jetty;

/**
 * Enumeration of the various web service components supported within Elements. Note, as these are intended to be used
 * on the command line, the normal upper case convention does not apply to maintain consistency with the rest of the
 * CLI options.
 */
public enum ElementsWebServiceComponent {

    /**
     * The core Elements API.
     */
    api,

    /**
     * The Documentation Service.
     */
    doc,

    /**
     * The Content Delivery (CDN) Service.
     */
    cdn,

    /**
     * The Code Service.
     */
    code,

    /**
     * The Application service, allows for interaction with the backend nodes.
     */
    app,

    /**
     * The Formidium Proxy service.
     */
    formidium_proxy

}
