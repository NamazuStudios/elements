package com.namazustudios.socialengine.rt.remote;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.Listenable;

import java.util.Set;

public interface ApplicationNodeAddressRegistry extends Listenable<ApplicationNodeAddressRegistryListener> {
    Object getAddressForHostAndPort(HostAndPort hostAndPort);

    HostAndPort getHostAndPortForAddress(Object address);

    Set<Object> getAllAddresses();

    Set<HostAndPort> getAllHostsAndPorts();
}
