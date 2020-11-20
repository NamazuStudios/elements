package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.stream.Stream;

public interface ExclusiveReadWriteTransaction extends ReadWriteTransaction {

    Stream<ResourceId> removeAllResources();

}
