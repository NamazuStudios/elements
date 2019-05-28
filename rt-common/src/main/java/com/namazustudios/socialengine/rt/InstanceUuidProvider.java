package com.namazustudios.socialengine.rt;

import sun.tools.jconsole.Worker;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


public interface InstanceUuidProvider {
    UUID getInstanceUuid();
}
