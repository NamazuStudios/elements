package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.dao.rt.DefaultContextFactory;
import com.namazustudios.socialengine.dao.rt.RTManifestDao;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.remote.jeromq.srv.SpotifySrvMonitor;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvMonitor;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.MultiplexedConnectionService;
import org.zeromq.ZContext;

import java.util.function.Function;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ManifestDao.class).to(RTManifestDao.class).asEagerSingleton();
        bind(ZContext.class).asEagerSingleton();
        bind(MultiplexedConnectionService.class).to(JeroMQMultiplexedConnectionService.class).asEagerSingleton();
        bind(SrvMonitor.class).to(SpotifySrvMonitor.class).asEagerSingleton();
        bind(ContextFactory.class).to(DefaultContextFactory.class).asEagerSingleton();
        bind(new TypeLiteral<Function<String, Context>>(){}).toProvider(RTContextProvider.class);
    }

}
