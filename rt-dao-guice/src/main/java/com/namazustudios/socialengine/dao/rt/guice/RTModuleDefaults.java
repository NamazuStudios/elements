package com.namazustudios.socialengine.dao.rt.guice;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.jeromq.ZContextProvider;

import java.util.Properties;

import static java.lang.Runtime.getRuntime;

public class RTModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(ZContextProvider.MAX_SOCKETS, "262144");
        properties.setProperty(ZContextProvider.IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        return properties;
    }

}
