package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.MultiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;


public class SimpleApplicationNodeMetadataContext implements ApplicationNodeMetadataContext {

    public static final String LOCAL_APPLICATION_NODE_UUID_FILE = ".local_application_node_uuid";

    private static final Logger logger = LoggerFactory.getLogger(SimpleApplicationNodeMetadataContext.class);

    private LoadMonitorService loadMonitorService;

    private ResourceService resourceService;

    final private UUID localApplicationNodeUuid;

    public SimpleApplicationNodeMetadataContext() {
        localApplicationNodeUuid = getOrCreateLocalApplicationNodeUuid();
    }

    private UUID getOrCreateLocalApplicationNodeUuid() {
        // TODO: set the location in properties
        File file = new File(LOCAL_APPLICATION_NODE_UUID_FILE);

        final UUID uuid;

        if (file.exists() && !file.isDirectory()) {
            try {
                final byte[] fileBytes = Files.readAllBytes(Paths.get(LOCAL_APPLICATION_NODE_UUID_FILE));
                uuid = UUID.nameUUIDFromBytes(fileBytes);
            }
            catch (final Exception e) {
                throw new MultiException("Failed to read local application node uuid file", Arrays.asList(e));
            }
        } else {
            uuid = UUID.randomUUID();
            try (PrintWriter out = new PrintWriter(LOCAL_APPLICATION_NODE_UUID_FILE)) {
                final String uuidString = uuid.toString();
                out.println(uuidString);
            }
            catch (Exception e) {
                throw new MultiException("Failed to write local application node uuid file", Arrays.asList(e));
            }
        }

        return uuid;
    }

    @Override
    public void start() {

        getLoadMonitorService().start();
    }

    @Override
    public void stop() {
        getLoadMonitorService().stop();
    }

    @Override
    public UUID getUuid() {
        return localApplicationNodeUuid;
    }

    @Override
    public long getInMemoryResourceCount() {
        return getResourceService().getInMemoryResourceCount();
    }

    @Override
    public double getLoadAverage() {
        return loadMonitorService.getLoadAverage();
    }


    public LoadMonitorService getLoadMonitorService() {
        return loadMonitorService;
    }

    @Inject
    public void setLoadMonitorService(LoadMonitorService loadMonitorService) {
        this.loadMonitorService = loadMonitorService;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }
}
