package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.dao.rt.DefaultContextFactory;
import com.namazustudios.socialengine.dao.rt.RTManifestDao;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQInstanceMetadataContext;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.srv.SpotifySrvMonitorService;
import com.namazustudios.socialengine.rt.srv.SrvMonitorService;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import org.zeromq.ZContext;

import java.util.function.Function;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ManifestDao.class);
        expose(ContextFactory.class);
// TODO Fix this
//        expose(ConnectionMultiplexer.class);
        expose(new TypeLiteral<Function<String, Context>>(){});

        install(new ZContextModule());

        bind(ManifestDao.class).to(RTManifestDao.class).asEagerSingleton();
        bind(ZContext.class).asEagerSingleton();
        bind(ConnectionService.class).to(JeroMQMultiplexedConnectionService.class).asEagerSingleton();
        bind(InstanceMetadataContext.class).to(JeroMQInstanceMetadataContext.class).asEagerSingleton();
        bind(SrvMonitorService.class).to(SpotifySrvMonitorService.class).asEagerSingleton();
        bind(ContextFactory.class).to(DefaultContextFactory.class).asEagerSingleton();
        bind(new TypeLiteral<Function<String, Context>>(){}).toProvider(RTContextProvider.class);

    }

}
