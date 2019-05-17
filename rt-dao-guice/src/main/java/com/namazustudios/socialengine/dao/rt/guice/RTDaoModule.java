package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.dao.rt.DefaultContextFactory;
import com.namazustudios.socialengine.dao.rt.RTManifestDao;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer;
import com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import org.zeromq.ZContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import static com.google.inject.name.Names.named;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(RTManifestDao.class);
        expose(RTContextProvider.class);

        install(new ZContextModule());

        bind(ManifestDao.class).to(RTManifestDao.class).asEagerSingleton();
        bind(ConnectionMultiplexer.class).to(JeroMQConnectionMultiplexer.class).asEagerSingleton();
        bind(ContextFactory.class).to(DefaultContextFactory.class).asEagerSingleton();
        bind(new TypeLiteral<Function<String, Context>>(){}).toProvider(RTContextProvider.class);

    }

}
