package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.appnode.Constants.CONTROL_REQUEST_TIMEOUT;
import static com.namazustudios.socialengine.appnode.Constants.STORAGE_BASE_DIRECTORY;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.JEROMQ_NODE_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.JEROMQ_NODE_MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.Constants.INSTANCE_DISCOVERY_SERVICE;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.HandlerContext.HANDLER_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.remote.PersistentInstanceIdProvider.INSTANCE_ID_FILE;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.HOST_INFO;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTable.UNIXFS_REVISION_TABLE_COUNT;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionJournal.UNIXFS_TRANSACTION_BUFFER_COUNT;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionJournal.UNIXFS_TRANSACTION_BUFFER_SIZE;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.UNIXFS_STORAGE_ROOT_DIRECTORY;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(CONTROL_REQUEST_TIMEOUT, "1000");
        properties.setProperty(SCHEDULER_THREADS, Integer.toString(Runtime.getRuntime().availableProcessors()) + 1);
        properties.setProperty(HANDLER_TIMEOUT_MSEC, "180000");
        properties.setProperty(STORAGE_BASE_DIRECTORY, "storage.xodus");
        properties.setProperty(INSTANCE_ID_FILE, "instance-id.txt");
        properties.setProperty(JEROMQ_CLUSTER_BIND_ADDRESS, "tcp://localhost:28883");
        properties.setProperty(INSTANCE_DISCOVERY_SERVICE, "STATIC");
        properties.setProperty(HOST_INFO, "tcp://localhost:28883");
        properties.setProperty(JEROMQ_NODE_MIN_CONNECTIONS, "10");
        properties.setProperty(JEROMQ_NODE_MAX_CONNECTIONS, "100");
        properties.setProperty(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.setProperty(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.setProperty(UNIXFS_REVISION_TABLE_COUNT, "8192");
        properties.setProperty(UNIXFS_TRANSACTION_BUFFER_SIZE, "4096");
        properties.setProperty(UNIXFS_TRANSACTION_BUFFER_COUNT, "8192");
        properties.setProperty(UNIXFS_STORAGE_ROOT_DIRECTORY, "storage.unixfs");
        return properties;
    }

}
