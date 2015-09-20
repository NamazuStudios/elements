package com.namazustudios.socialengine.rt.guice.example.Client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.guice.EdgeFilterListModule;
import com.namazustudios.socialengine.rt.guice.ExceptionMapperModule;
import com.namazustudios.socialengine.rt.guice.SimpleClientModule;
import com.namazustudios.socialengine.rt.guice.SimpleServerModule;
import com.namazustudios.socialengine.rt.lua.FQNTypeRegistry;
import com.namazustudios.socialengine.rt.lua.TypeRegistry;
import com.namazustudios.socialengine.rt.lua.guice.ClasspathScanningLuaResourceModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaServerModule;
import org.apache.mina.core.service.IoAcceptor;

import java.io.Console;
import java.net.InetSocketAddress;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class ClientMain {


    public static void main(final String[] args) throws Exception {
        final Injector injector = Guice.createInjector(
                new SimpleClientModule()
        );

    }

}
