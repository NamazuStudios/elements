package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.JEROMQ_NODE_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.JEROMQ_NODE_MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.Constants.INSTANCE_DISCOVERY_SERVICE;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.HandlerContext.HANDLER_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.git.Constants.GIT_STORAGE_DIRECTORY;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.IO_THREADS;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.MAX_SOCKETS;
import static com.namazustudios.socialengine.rt.remote.PersistentInstanceIdProvider.INSTANCE_ID_FILE;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.SpotifySrvInstanceDiscoveryService.SRV_QUERY;
import static com.namazustudios.socialengine.rt.remote.SpotifySrvInstanceDiscoveryService.SRV_SERVERS;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule.DiscoveryType.STATIC;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTable.UNIXFS_REVISION_TABLE_COUNT;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionJournal.UNIXFS_TRANSACTION_BUFFER_COUNT;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionJournal.UNIXFS_TRANSACTION_BUFFER_SIZE;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.UNIXFS_STORAGE_ROOT_DIRECTORY;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistence.RESOURCE_BLOCK_SIZE;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistence.RESOURCE_ENVIRONMENT_PATH;
import static com.namazustudios.socialengine.rt.xodus.XodusSchedulerContext.SCHEDULER_ENVIRONMENT_PATH;
import static java.lang.Runtime.getRuntime;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(SCHEDULER_THREADS, Integer.toString(getRuntime().availableProcessors()) + 1);
        properties.setProperty(HANDLER_TIMEOUT_MSEC, "180000");
        properties.setProperty(JEROMQ_CLUSTER_BIND_ADDRESS, "tcp://localhost:28883");
        properties.setProperty(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS, "10");
        properties.setProperty(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.setProperty(STATIC_HOST_INFO, "tcp://localhost:28883");
        properties.setProperty(JEROMQ_NODE_MIN_CONNECTIONS, "10");
        properties.setProperty(JEROMQ_NODE_MAX_CONNECTIONS, "100");
        properties.setProperty(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.setProperty(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.setProperty(UNIXFS_REVISION_TABLE_COUNT, "8192");
        properties.setProperty(UNIXFS_TRANSACTION_BUFFER_SIZE, "4096");
        properties.setProperty(UNIXFS_TRANSACTION_BUFFER_COUNT, "8192");
        properties.setProperty(RESOURCE_BLOCK_SIZE, "4096");
        properties.setProperty(INSTANCE_ID_FILE, "script-storage/instance-id.txt");
        properties.setProperty(RESOURCE_ENVIRONMENT_PATH, "script-storage/storage.xodus.resources");
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
