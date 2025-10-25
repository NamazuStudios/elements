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
     * The jrpc API.
     */
    jrpc,

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
     * The Application node.
     */
    app_node,

    /**
     * The Application server.
     */
    app_serve,

    /**
     * The Web UI Admin Panel for Elements.
     */
    web_ui,
}
