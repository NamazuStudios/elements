package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ClientInstance implements Instance {

    private static final Logger logger = LoggerFactory.getLogger(ClientInstance.class);

    private InstanceId instanceId;

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

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    @Override
    public void close() {

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getInstanceDiscoveryService().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping InstanceDiscoveryService.", ex);
        }

        try {
            getInstanceConnectionService().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception stopping InstanceConnectionService.", ex);
        }

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
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
