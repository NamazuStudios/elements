package dev.getelements.elements.service;

import dev.getelements.elements.test.EmbeddedTestService;

public class UnixFSIntegrationTestModule extends AbstractIntegrationTestModule {

    @Override
    public EmbeddedTestService embeddedTestService(final int mongoPort, final int redisPort, final int nodePort) {
        // JeroMQ removed (issue #10). This embedded-cluster harness was backed by
        // JeroMQEmbeddedTestService, which no longer exists. Pending a Fabric-backed
        // replacement; see the three Test*SmartContractInvocationService classes, whose
        // @Factory methods are disabled rather than constructing this module.
        throw new UnsupportedOperationException(
                "JeroMQ removed; no embedded test cluster harness is available (see issue #10)");
    }

}
