package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.dao.rt.DefaultContextFactory;
import com.namazustudios.socialengine.dao.rt.RTManifestDao;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.rt.*;
//import com.namazustudios.socialengine.rt.srv.SpotifySrvMonitorService;
//import com.namazustudios.socialengine.rt.srv.SrvMonitorService;
import org.zeromq.ZContext;

import javax.inject.Named;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.Constants.IS_LOCAL_ENVIRONMENT_NAME;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModule extends PrivateModule {
    private boolean isLocalInstance;

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
//        bind(SrvMonitorService.class).to(SpotifySrvMonitorService.class).asEagerSingleton();
        bind(ContextFactory.class).to(DefaultContextFactory.class).asEagerSingleton();
        bind(new TypeLiteral<Function<String, Context>>(){}).toProvider(RTContextProvider.class);

// TODO
//        if (isLocalInstance) {
//            bind(InstanceDiscoveryService.class)
//                    .to(StaticInstanceDiscoveryService.class)
//                    .asEagerSingleton();
//        }
//        else {
//            bind(InstanceDiscoveryService.class)
//                    .to(SrvInstanceDiscoveryService.class)
//                    .asEagerSingleton();
//        }

    }

    public boolean getLocalInstance() {
        return isLocalInstance;
    }

    @Inject
    @Named(IS_LOCAL_ENVIRONMENT_NAME)
    public void setLocalInstance(boolean localInstance) {
        isLocalInstance = localInstance;
    }
}
