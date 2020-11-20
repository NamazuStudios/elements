package com.namazustudios.socialengine.rt;

import javax.inject.Provider;
import java.util.UUID;


public interface InstanceUuidProvider extends Provider<UUID> {
    UUID get();
}
