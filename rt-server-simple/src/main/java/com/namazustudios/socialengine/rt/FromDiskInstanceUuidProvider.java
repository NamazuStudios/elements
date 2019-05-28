package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.MultiException;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;


public class FromDiskInstanceUuidProvider implements InstanceUuidProvider {
    public static final String LOCAL_APPLICATION_NODE_UUID_FILE = ".local_application_node_uuid";

    private final UUID instanceUuid;

    public FromDiskInstanceUuidProvider() {
        instanceUuid = getInstanceUuid();
    }

    public UUID get() {
        return instanceUuid;
    }

    private UUID getInstanceUuid() {
        // TODO: set the location in properties
        File file = new File(LOCAL_APPLICATION_NODE_UUID_FILE);

        final UUID instanceUuid;

        if (file.exists() && !file.isDirectory()) {
            try {
                final byte[] fileBytes = Files.readAllBytes(Paths.get(LOCAL_APPLICATION_NODE_UUID_FILE));
                instanceUuid = UUID.nameUUIDFromBytes(fileBytes);
            }
            catch (final Exception e) {
                throw new MultiException("Failed to read local application node uuid file", Arrays.asList(e));
            }
        } else {
            instanceUuid = UUID.randomUUID();
            try (PrintWriter out = new PrintWriter(LOCAL_APPLICATION_NODE_UUID_FILE)) {
                final String uuidString = instanceUuid.toString();
                out.println(uuidString);
            }
            catch (Exception e) {
                throw new MultiException("Failed to write local application node uuid file", Arrays.asList(e));
            }
        }

        return instanceUuid;
    }

}
