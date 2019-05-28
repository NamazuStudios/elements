package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Listenable;
import com.namazustudios.socialengine.rt.SrvUniqueIdentifier;

import java.util.Set;

public interface ApplicationNodeAddressRegistry extends Listenable<ApplicationNodeAddressRegistryListener> {
    Object getAddressForSrvUniqueIdentifier(SrvUniqueIdentifier srvUniqueIdentifier);

    SrvUniqueIdentifier getSrvUniqueIdentifierForAddress(Object address);

    Set<Object> getAllAddresses();

    Set<SrvUniqueIdentifier> getAllSrvUniqueIdentifiers();
}
