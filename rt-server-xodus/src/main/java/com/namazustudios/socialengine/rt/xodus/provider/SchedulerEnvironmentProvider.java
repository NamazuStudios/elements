package com.namazustudios.socialengine.rt.xodus.provider;

import com.namazustudios.socialengine.rt.util.ProxyDelegate;
import jetbrains.exodus.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.namazustudios.socialengine.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT;

public class SchedulerEnvironmentProvider implements Provider<Environment> {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerEnvironmentProvider.class);

    private Provider<ProxyDelegate<Environment>> proxyDelegateProvider;

    @Override
    public Environment get() {
        return getProxyDelegateProvider().get().getProxy();
    }

    public Provider<ProxyDelegate<Environment>> getProxyDelegateProvider() {
        return proxyDelegateProvider;
    }

    @Inject
    public void setProxyDelegateProvider(@Named(SCHEDULER_ENVIRONMENT) Provider<ProxyDelegate<Environment>> proxyDelegateProvider) {
        this.proxyDelegateProvider = proxyDelegateProvider;
    }

}

