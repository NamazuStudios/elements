package com.namazustudios.socialengine.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.namazustudios.socialengine.rt.Listenable;
import com.namazustudios.socialengine.rt.SrvUniqueIdentifier;
import com.namazustudios.socialengine.rt.remote.ApplicationNodeAddressRegistry;
import com.namazustudios.socialengine.rt.remote.ApplicationNodeAddressRegistryListener;
import com.namazustudios.socialengine.rt.srv.SrvMonitorService;
import com.namazustudios.socialengine.rt.srv.SrvMonitorServiceListener;
import com.namazustudios.socialengine.rt.srv.SrvRecord;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class JeroMQApplicationNodeAddressRegistry implements ApplicationNodeAddressRegistry, SrvMonitorServiceListener {
    // TODO: define expected size (i.e. expected number of application nodes in the network) in properties
    private final AtomicReference<BiMap<Object, SrvUniqueIdentifier>> atomicAddressBiMapReference =
            new AtomicReference<>(HashBiMap.create(16));

    private final AtomicReference<Set<ApplicationNodeAddressRegistryListener>>
            atomicApplicationNodeAddressRegistryListenersReference = new AtomicReference<>(new HashSet<>());

    private SrvMonitorService srvMonitorService;


    @Override
    public Object getAddressForSrvUniqueIdentifier(SrvUniqueIdentifier srvUniqueIdentifier) {
        BiMap<Object, SrvUniqueIdentifier> addressBiMap = atomicAddressBiMapReference.get();
        return addressBiMap.inverse().get(srvUniqueIdentifier);
    }

    @Override
    public SrvUniqueIdentifier getSrvUniqueIdentifierForAddress(Object address) {
        BiMap<Object, SrvUniqueIdentifier> addressBiMap = atomicAddressBiMapReference.get();
        return addressBiMap.get(address);
    }

    @Override
    public Set<Object> getAllAddresses() {
        BiMap<Object, SrvUniqueIdentifier> addressBiMap = atomicAddressBiMapReference.get();
        return ImmutableSet.copyOf(addressBiMap.keySet());
    }

    @Override
    public Set<SrvUniqueIdentifier> getAllSrvUniqueIdentifiers() {
        BiMap<Object, SrvUniqueIdentifier> addressBiMap = atomicAddressBiMapReference.get();
        return ImmutableSet.copyOf(addressBiMap.values());
    }

    private void registerAsSrvMonitorServiceListener() {
        if (srvMonitorService != null) {
            srvMonitorService.registerListener(this);
        }
    }

    public void onSrvRecordCreated(SrvRecord srvRecord) {

    }

    public void onSrvRecordUpdated(SrvRecord srvRecord) {
        // for now, do nothing
    }

    public void onSrvRecordDeleted(SrvRecord srvRecord) {

    }

    public void registerListener(ApplicationNodeAddressRegistryListener listener) {
        Set<ApplicationNodeAddressRegistryListener> applicationNodeAddressRegistryListeners =
                atomicApplicationNodeAddressRegistryListenersReference.get();

        applicationNodeAddressRegistryListeners.add(listener);
    }

    public boolean unregisterListener(ApplicationNodeAddressRegistryListener listener) {
        Set<ApplicationNodeAddressRegistryListener> applicationNodeAddressRegistryListeners =
                atomicApplicationNodeAddressRegistryListenersReference.get();

        return applicationNodeAddressRegistryListeners.remove(listener);
    }

    public Set<ApplicationNodeAddressRegistryListener> getListeners() {
        Set<ApplicationNodeAddressRegistryListener> applicationNodeAddressRegistryListeners =
                atomicApplicationNodeAddressRegistryListenersReference.get();

        return ImmutableSet.copyOf(applicationNodeAddressRegistryListeners);
    }

    public SrvMonitorService getSrvMonitorService() {
        return srvMonitorService;
    }

    @Inject
    public void setSrvMonitorService(SrvMonitorService srvMonitorService) {
        this.srvMonitorService = srvMonitorService;
        registerAsSrvMonitorServiceListener();
    }
}
