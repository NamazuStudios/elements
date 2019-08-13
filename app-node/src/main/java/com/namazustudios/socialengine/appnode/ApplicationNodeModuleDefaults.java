package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.appnode.Constants.CONTROL_REQUEST_TIMEOUT;
import static com.namazustudios.socialengine.appnode.Constants.STORAGE_BASE_DIRECTORY;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.HandlerContext.HANDLER_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.jeromq.ConnectionPool.*;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(TIMEOUT, "60");
        properties.setProperty(MIN_CONNECTIONS, "10");
        properties.setProperty(MAX_CONNECTIONS, "10000");
        properties.setProperty(CONTROL_REQUEST_TIMEOUT, "1000");
        properties.setProperty(SCHEDULER_THREADS, Integer.toString(Runtime.getRuntime().availableProcessors()) + 1);
        properties.setProperty(HANDLER_TIMEOUT_MSEC, "180000");
        properties.setProperty(STORAGE_BASE_DIRECTORY, "storage.xodus");
        return properties;
    }

}
