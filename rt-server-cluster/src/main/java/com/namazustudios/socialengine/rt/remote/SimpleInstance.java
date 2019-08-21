package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleInstance implements Instance {

    private static final Logger logger = LoggerFactory.getLogger(SimpleInstance.class);

    protected InstanceId instanceId;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    private InstanceDiscoveryService instanceDiscoveryService;

    private InstanceConnectionService instanceConnectionService;

    @Override
    public void start() {

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getInstanceDiscoveryService().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting InstanceDiscoveryService.", ex);
        }

        try {
            getInstanceConnectionService().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting InstanceConnectionService.", ex);
        }

        try {
            getRemoteInvokerRegistry().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting RemoteInvokerRegistry.", ex);
        }

        postStart(exceptionList::add);

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    protected void postStart(final Consumer<Exception> exceptionConsumer) {}

    @Override
    public void close() {

        final List<Exception> exceptionList = new ArrayList<>();

        preClose(exceptionList::add);

        try {
            getRemoteInvokerRegistry().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping RemoteInvokerRegistry.", ex);
        }

        try {
            getInstanceConnectionService().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping InstanceDiscoveryService.", ex);
        }

        try {
            getInstanceDiscoveryService().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping InstanceDiscoveryService.", ex);
        }

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    protected void preClose(final Consumer<Exception> exceptionConsumer) {}

    @Override
    public void refreshConnections() {
        getInstanceConnectionService().refresh();
        getRemoteInvokerRegistry().refresh();
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    public InstanceConnectionService getInstanceConnectionService() {
        return instanceConnectionService;
    }

    @Inject
    public void setInstanceConnectionService(InstanceConnectionService instanceConnectionService) {
        this.instanceConnectionService = instanceConnectionService;
    }

}
