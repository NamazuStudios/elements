package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.srv.SrvMonitorService;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class JeroMQRemoteInvokerRegistry implements RemoteInvokerRegistry {
    private final AtomicReference<Map<Object, RemoteInvoker>> atomicRemoteInvokerMapReference = new AtomicReference<>(new HashMap<>());

    private SrvMonitorService srvMonitorService;


    private void registerAsSrvMonitorListener() {
        srvMonitorService.registerOnCreatedSrvRecordListener((srvRecord -> {
            // 1) get the advertised addr from the app node listed in the srv record,
            // 2) create remote invoker from guice, and then
            // 3) call registerRemoteInvoker()
        }));

        srvMonitorService.registerOnUpdatedSrvRecordListener((srvRecord -> {
            // unused for now
        }));

        srvMonitorService.registerOnDeletedSrvRecordListener((srvRecord -> {
            //
        }));
    }

    private void registerRemoteInvoker(Object address, RemoteInvoker remoteInvoker) {
        final Map<Object, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        remoteInvokerMap.put(address, remoteInvoker);
    }

    private boolean unregisterRemoteInvoker(Object address) {
        final Map<Object, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        if (remoteInvokerMap.containsKey(address)) {
            remoteInvokerMap.remove(address);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public RemoteInvoker getRemoteInvoker(Object address) {
        final Map<Object, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        return remoteInvokerMap.get(address);
    }

    @Override
    public Set<RemoteInvoker> getAllRemoteInvokers() {
        final Map<Object, RemoteInvoker> remoteInvokerMap = atomicRemoteInvokerMapReference.get();
        final Set<RemoteInvoker> remoteInvokers = remoteInvokerMap.values().stream().collect(Collectors.toSet());
        return remoteInvokers;
    }

    public SrvMonitorService getSrvMonitorService() {
        return srvMonitorService;
    }

    @Inject
    public void setSrvMonitorService(SrvMonitorService srvMonitorService) {
        this.srvMonitorService = srvMonitorService;

        if (this.srvMonitorService != null) {
            registerAsSrvMonitorListener();
        }
    }
}