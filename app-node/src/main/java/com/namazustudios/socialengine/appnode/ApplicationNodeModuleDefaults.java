package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.HandlerContext;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import java.util.Properties;

import static com.namazustudios.socialengine.appnode.Constants.*;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.BIND_ADDR;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.CONTROL_BIND_ADDR;
import static com.namazustudios.socialengine.rt.Constants.*;
import static com.namazustudios.socialengine.rt.HandlerContext.*;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(ConnectionPool.TIMEOUT, "60");
        properties.setProperty(ConnectionPool.MIN_CONNECTIONS, "1000");
        properties.setProperty(ConnectionPool.MAX_CONNECTIONS, "450000");
        properties.setProperty(BIND_ADDR, "tcp://*:28883");
        properties.setProperty(CONTROL_BIND_ADDR, "tcp://*:20883");
        properties.setProperty(CONTROL_REQUEST_TIMEOUT, "1000");
        properties.setProperty(SCHEDULER_THREADS, Integer.toString(Runtime.getRuntime().availableProcessors()) + 1);
        properties.setProperty(HANDLER_TIMEOUT_MSEC, "180000");
        properties.setProperty(INSTANCE_ID_FILE, "instance-id.txt");
        properties.setProperty(JEROMQ_CLUSTER_BIND_ADDRESS, "tcp://localhost:28883");
        properties.setProperty(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS, "10");
        properties.setProperty(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.setProperty(HOST_INFO, "tcp://localhost:28883");
        properties.setProperty(JEROMQ_NODE_MIN_CONNECTIONS, "10");
        properties.setProperty(JEROMQ_NODE_MAX_CONNECTIONS, "100");
        properties.setProperty(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.setProperty(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.setProperty(UNIXFS_REVISION_TABLE_COUNT, "8192");
        properties.setProperty(UNIXFS_TRANSACTION_BUFFER_SIZE, "4096");
        properties.setProperty(UNIXFS_TRANSACTION_BUFFER_COUNT, "8192");
        properties.setProperty(SCHEDULER_ENVIRONMENT_PATH, "script-storage/storage.xodus.scheduler");
        properties.setProperty(UNIXFS_STORAGE_ROOT_DIRECTORY, "script-storage/storage.unixfs");
        properties.setProperty(IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.setProperty(MAX_SOCKETS, "500000");
        properties.setProperty(GIT_STORAGE_DIRECTORY, "script-repos/git");
        properties.setProperty(SRV_QUERY, "_elements._tcp.internal");
        properties.setProperty(SRV_SERVERS, "");
        return properties;
    }

}
