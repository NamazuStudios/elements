package dev.getelements.elements.appnode.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.annotation.RemoteScope;
import dev.getelements.elements.rt.guice.SimpleInstanceMetadataContextModule;
import dev.getelements.elements.rt.guice.SimpleLoadMonitorServiceModule;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.remote.LocalInvocationDispatcher;
import dev.getelements.elements.rt.remote.MasterNodeLifecycle;
import dev.getelements.elements.rt.remote.MasterNodeLocalInvocationDispatcher;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.NodeLifecycle;
import dev.getelements.elements.rt.remote.guice.NodeIdModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQNodeModule;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.annotation.RemoteScope.*;
import static dev.getelements.elements.rt.remote.Node.MASTER_NODE_NAME;

public class MasterNodeModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new SimpleLoadMonitorServiceModule());
        install(new SimpleInstanceMetadataContextModule());
        install(NodeIdModule.forMasterNode(getProvider(InstanceId.class)));

        install(new JeroMQNodeModule()
            .withNodeName("MasterNode")
        );

        bind(Node.class)
                .annotatedWith(named(MASTER_NODE_NAME))
                .to(Node.class);

        bind(NodeLifecycle.class)
                .to(MasterNodeLifecycle.class)
                .asEagerSingleton();

        bind(LocalInvocationDispatcher.class)
                .to(MasterNodeLocalInvocationDispatcher.class)
                .asEagerSingleton();

        bind(String.class)
                .annotatedWith(named(REMOTE_SCOPE))
                .toInstance(MASTER_SCOPE);

        bind(String.class)
                .annotatedWith(named(REMOTE_PROTOCOL))
                .toInstance(ELEMENTS_RT_PROTOCOL);

        expose(Key.get(Node.class, named(MASTER_NODE_NAME)));

    }

}
