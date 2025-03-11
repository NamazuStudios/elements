package dev.getelements.elements.appnode;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.remote.jeromq.JeroMQNode.JEROMQ_NODE_MAX_CONNECTIONS;
import static dev.getelements.elements.remote.jeromq.JeroMQNode.JEROMQ_NODE_MIN_CONNECTIONS;
import static dev.getelements.elements.rt.Constants.*;
import static dev.getelements.elements.rt.HandlerContext.HANDLER_TIMEOUT_MSEC;
import static dev.getelements.elements.rt.git.FileSystemScriptStorageGitLoaderProvider.ELEMENT_STORAGE_DIRECTORY;
import static dev.getelements.elements.rt.jeromq.JeroMQAsyncConnectionService.ASYNC_CONNECTION_IO_THREADS;
import static dev.getelements.elements.rt.jeromq.ZContextProvider.*;
import static dev.getelements.elements.rt.remote.JndiSrvInstanceDiscoveryService.SRV_AUTHORITATIVE;
import static dev.getelements.elements.rt.remote.PersistentInstanceIdProvider.INSTANCE_ID_FILE;
import static dev.getelements.elements.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static dev.getelements.elements.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.*;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.REFRESH_RATE_SECONDS;
import static dev.getelements.elements.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static dev.getelements.elements.rt.remote.guice.InstanceDiscoveryServiceModule.DiscoveryType.STATIC;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQSecurityProvider.JEROMQ_ALLOW_PLAIN_TRAFFIC;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQSecurityProvider.JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionJournal.UNIXFS_TRANSACTION_BUFFER_SIZE;
import static dev.getelements.elements.rt.transact.unix.UnixFSUtils.UNIXFS_STORAGE_ROOT_DIRECTORY;
import static java.lang.Runtime.getRuntime;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(SCHEDULER_THREADS, Integer.toString(getRuntime().availableProcessors()) + 1);
        properties.setProperty(HANDLER_TIMEOUT_MSEC, "180000");
        properties.setProperty(JEROMQ_CLUSTER_BIND_ADDRESS, "tcp://*:28883");
        properties.setProperty(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS, "10");
        properties.setProperty(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.setProperty(STATIC_HOST_INFO, "tcp://localhost:28883");
        properties.setProperty(JEROMQ_NODE_MIN_CONNECTIONS, "10");
        properties.setProperty(JEROMQ_NODE_MAX_CONNECTIONS, "100");
        properties.setProperty(JEROMQ_ALLOW_PLAIN_TRAFFIC, "true");
        properties.setProperty(JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE, "");
        properties.setProperty(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.setProperty(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.setProperty(UNIXFS_TRANSACTION_BUFFER_SIZE, "32768");
        properties.setProperty(INSTANCE_ID_FILE, "script-storage/instance-id.txt");
        properties.setProperty(UNIXFS_STORAGE_ROOT_DIRECTORY, "script-storage/storage.unixfs");
        properties.setProperty(IPV6, "true");
        properties.setProperty(IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.setProperty(ASYNC_CONNECTION_IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.setProperty(MAX_SOCKETS, "500000");
        properties.setProperty(ELEMENT_STORAGE_DIRECTORY, "script-repos/git");
        properties.setProperty(SRV_QUERY, "_elements._tcp.internal");
        properties.setProperty(SRV_SERVERS, "");
        properties.setProperty(SRV_AUTHORITATIVE, "false");
        properties.setProperty(REFRESH_RATE_SECONDS, String.valueOf(DEFAULT_REFRESH_RATE));
        properties.setProperty(REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_REFRESH_TIMEOUT));
        properties.setProperty(TOTAL_REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_TOTAL_REFRESH_TIMEOUT));
        return properties;
    }

}
