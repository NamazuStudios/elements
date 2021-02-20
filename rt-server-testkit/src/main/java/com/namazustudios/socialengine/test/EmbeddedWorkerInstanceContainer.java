package com.namazustudios.socialengine.test;

import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.Worker;

public interface EmbeddedWorkerInstanceContainer extends EmbeddedInstanceContainer {

    /**
     * Gets the {@link IocResolver} for the {@link Node} associated with the supplied {@link NodeId}
     *
     * @param applicationId
     * @return the {@link NodeId}
     */
    IocResolver getIocResolver(NodeId applicationId);

    /**
     * Enables the default HTTP client for the {@link Worker} contained in this container.
     * s
     * @return this instance
     */
    EmbeddedWorkerInstanceContainer withDefaultHttpClient();

    /**
     * Returns the default Worker IoC resolver. The definition of the default worker is implementation specific.
     * Typically, this is the fist configured {@link ApplicationId}
     *
     * @return the default worker IoC resolver.
     */
    IocResolver getIocResolver();

    /**
     * Gets the {@link IocResolver} for the {@link Node} associated with the supplied {@link NodeId}
     *
     * @param applicationId
     * @return the {@link NodeId}
     */
    default IocResolver getIocResolver(final ApplicationId applicationId) {
        final var nodeId = NodeId.forInstanceAndApplication(getInstanceId(), applicationId);
        return getIocResolver(nodeId);
    }

}
