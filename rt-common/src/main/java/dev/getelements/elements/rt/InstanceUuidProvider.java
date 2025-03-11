package dev.getelements.elements.rt;

import jakarta.inject.Provider;
import java.util.UUID;


public interface InstanceUuidProvider extends Provider<UUID> {
    UUID get();
}
