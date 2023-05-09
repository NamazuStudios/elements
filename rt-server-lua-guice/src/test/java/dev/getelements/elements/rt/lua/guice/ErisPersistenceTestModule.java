package dev.getelements.elements.rt.lua.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.guice.GuiceIoCResolver;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.RemoteInvokerRegistry;

import javax.ws.rs.client.Client;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;
import static dev.getelements.elements.rt.Context.REMOTE;
import static dev.getelements.elements.rt.id.NodeId.randomNodeId;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ErisPersistenceTestModule extends AbstractModule {

    @Override
    protected void configure() {

        install(new LuaModule());

        final NodeId nodeId = randomNodeId();
        bind(NodeId.class).toInstance(nodeId);

        // Types backed by actual implementations
        bind(IocResolver.class).to(GuiceIoCResolver.class).asEagerSingleton();
        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();
        bind(TestJavaEvent.class).toInstance(mock(TestJavaEvent.class));
        install(new ClasspathAssetLoaderModule().withDefaultPackageRoot());

        // Types that are mocks

        bind(Client.class).toInstance(mock(Client.class));
        bind(PersistenceStrategy.class).toInstance(mock(PersistenceStrategy.class));

        install(new AbstractModule() {
            @Override
            protected void configure() {
                final var mockRemoteInvokerRegistry = mock(RemoteInvokerRegistry.class);
                bind(RemoteInvokerRegistry.class).toInstance(mockRemoteInvokerRegistry);
            }
        });

        final var localContext = mockContext();
        final var remoteContext = mockContext();
        bind(Context.class).annotatedWith(named(LOCAL)).toInstance(localContext);
        bind(Context.class).annotatedWith(named(REMOTE)).toInstance(remoteContext);

    }

    private Context mockContext() {
        final var context = mock(Context.class);
        final var taskContext = mock(TaskContext.class);
        doReturn(taskContext).when(context).getTaskContext();
        return context;
    }

}
