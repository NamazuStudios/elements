package dev.getelements.elements.test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.remote.guice.SimpleInstanceModule;
import dev.getelements.elements.rt.remote.jeromq.JeroMQSecurity;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;
import dev.getelements.elements.test.guice.TestClientContextFactoryModule;
import org.zeromq.ZContext;

public class JeroMQEmbeddedClientInstanceContainer extends JeroMQEmbeddedInstanceContainer
                                                   implements EmbeddedClientInstanceContainer{

    public JeroMQEmbeddedClientInstanceContainer() {
        withInstanceModules(new AbstractModule() {
            @Override
            protected void configure() {
                install(new SimpleInstanceModule());
                install(new TestClientContextFactoryModule());
                install(new JeroMQInstanceConnectionServiceModule()
                    .withBindAddress("")
                    .withDefaultRefreshInterval()
                );
            }
        });
    }

    @Override
    public Context.Factory getContextFactory() {
        return getInjector().getInstance(Context.Factory.class);
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer withInstanceId(InstanceId instanceId) {
        return (JeroMQEmbeddedClientInstanceContainer) super.withInstanceId(instanceId);
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer withInstanceModules(Module module) {
        return (JeroMQEmbeddedClientInstanceContainer) super.withInstanceModules(module);
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer withInstanceModules(Module module, Module... modules) {
        return (JeroMQEmbeddedClientInstanceContainer) super.withInstanceModules(module, modules);
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer clearConnectAddresses() {
        return (JeroMQEmbeddedClientInstanceContainer) super.clearConnectAddresses();
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer withConnectAddress(String address) {
        return (JeroMQEmbeddedClientInstanceContainer) super.withConnectAddress(address);
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer withConnectAddress(String address, String... addresses) {
        return (JeroMQEmbeddedClientInstanceContainer) super.withConnectAddress(address, addresses);
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer withZContext(ZContext zContext) {
        return (JeroMQEmbeddedClientInstanceContainer) super.withZContext(zContext);
    }

    @Override
    public JeroMQEmbeddedClientInstanceContainer withSecurity(JeroMQSecurity jeroMQSecurity) {
        return (JeroMQEmbeddedClientInstanceContainer) super.withSecurity(jeroMQSecurity);
    }

}
