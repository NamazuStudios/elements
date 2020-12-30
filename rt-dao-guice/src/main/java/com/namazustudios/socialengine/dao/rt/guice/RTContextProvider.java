//package com.namazustudios.socialengine.dao.rt.guice;
//
//import com.google.inject.Injector;
//import com.namazustudios.socialengine.dao.ApplicationDao;
//import com.namazustudios.socialengine.model.application.Application;
//import com.namazustudios.socialengine.rt.Context;
//import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
//import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQContextModule;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.util.function.Function;
//
//public class RTContextProvider implements Provider<Function<String, Context>> {
//
//    private Provider<Injector> injectorProvider;
//
//    private Provider<InstanceConnectionService> connectionServiceProvider;
//
//    private Provider<ApplicationDao> applicationDaoProvider;
//
//    @Override
//    public Function<String, Context> get() {
//        return applicationId -> {
//
//            final ApplicationDao applicationDao = getApplicationDaoProvider().get();
//            final Application application = applicationDao.getActiveApplication(applicationId);
//
//            final JeroMQContextModule jeroMQClientModule = new JeroMQContextModule()
//                .withApplicationUniqueName(application.getId());
//
//            final Injector contextInjector = getInjectorProvider()
//                .get()
//                .createChildInjector(jeroMQClientModule, jeroMQClientModule);
//
//            final Context context = contextInjector.getInstance(Context.class);
//
//            context.start();
//
//            return context;
//
//        };
//    }
//
//    public Provider<Injector> getInjectorProvider() {
//        return injectorProvider;
//    }
//
//    @Inject
//    public void setInjectorProvider(Provider<Injector> injectorProvider) {
//        this.injectorProvider = injectorProvider;
//    }
//
//    public Provider<InstanceConnectionService> getConnectionServiceProvider() {
//        return connectionServiceProvider;
//    }
//
//    @Inject
//    public void setConnectionServiceProvider(Provider<InstanceConnectionService> connectionServiceProvider) {
//        this.connectionServiceProvider = connectionServiceProvider;
//    }
//
//    public Provider<ApplicationDao> getApplicationDaoProvider() {
//        return applicationDaoProvider;
//    }
//
//    @Inject
//    public void setApplicationDaoProvider(Provider<ApplicationDao> applicationDaoProvider) {
//        this.applicationDaoProvider = applicationDaoProvider;
//    }
//
//}
