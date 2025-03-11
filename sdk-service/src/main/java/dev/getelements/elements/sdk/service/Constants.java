package dev.getelements.elements.sdk.service;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementPublic;

/**
 * Service layer constants.
 */
@ElementPublic
public class Constants {

    private Constants() {}

    /**
     * Names a service as unscoped.
     */
    public static final String UNSCOPED = "dev.getelements.elements.service.unscoped";

    /**
     * Used to specify the RPC provider for bsc blockchain.
     */
    @ElementDefaultAttribute("https://data-seed-prebsc-1-s1.binance.org:8545")
    public static final String BSC_RPC_PROVIDER = "dev.getelements.elements.blockchain.bsc.provider";

    /**
     * Used to specify the session timeout, in seconds
     */
    @ElementDefaultAttribute("172800")
    public static final String SESSION_TIMEOUT_SECONDS = "dev.getelements.elements.session.timeout.seconds";

    /**
     * Used to specify the mock session timeout.
     */
    @ElementDefaultAttribute("3600")
    public static final String MOCK_SESSION_TIMEOUT_SECONDS = "dev.getelements.elements.mock.session.timeout.seconds";

    /**
     * Used to specify the host for neo blockchain.
     */
    @ElementDefaultAttribute("http://127.0.0.1")
    public static final String NEO_BLOCKCHAIN_HOST = "dev.getelements.elements.blockchain.neo.host";

    /**
     * Used to specify the port for neo blockchain.
     */
    @ElementDefaultAttribute("50012")
    public static final String NEO_BLOCKCHAIN_PORT = "dev.getelements.elements.blockchain.neo.port";

    /**
     * Used to specify the file path for static content.
     */
    @ElementDefaultAttribute("content")
    public static final String CDN_FILE_DIRECTORY = "dev.getelements.elements.cdnserve.storage.directory";

    /**
     * Used to specify the endpoint file path for cloning static content.
     */
    @ElementDefaultAttribute("clone")
    public static final String CDN_CLONE_ENDPOINT = "dev.getelements.elements.cdnserve.endpoint.clone";

    /**
     * Used to specify the endpoint for serving static content.
     */
    @ElementDefaultAttribute("serve")
    public static final String CDN_SERVE_ENDPOINT = "dev.getelements.elements.cdnserve.endpoint.serve";

}
