package com.namazustudios.socialengine.remote;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.RemoteProxyProvider;
import org.mockito.Mockito;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = RemoteProxyProviderUnitTest.Module.class)
public class RemoteProxyProviderUnitTest {

    private RemoteInvoker mockRemoteInvoker;

    private MockServiceInterface mockServiceInterface;

    @Test
    public void testSync() {

    }

    public RemoteInvoker getMockRemoteInvoker() {
        return mockRemoteInvoker;
    }

    @Inject
    public void setMockRemoteInvoker(RemoteInvoker mockRemoteInvoker) {
        this.mockRemoteInvoker = mockRemoteInvoker;
    }

    public MockServiceInterface getMockServiceInterface() {
        return mockServiceInterface;
    }

    @Inject
    public void setMockServiceInterface(MockServiceInterface mockServiceInterface) {
        this.mockServiceInterface = mockServiceInterface;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            final RemoteInvoker remoteInvoker = Mockito.mock(RemoteInvoker.class);
            bind(RemoteInvoker.class).toInstance(remoteInvoker);
            bind(MockServiceInterface.class).toProvider(new RemoteProxyProvider<>(MockServiceInterface.class));
        }

    }

}
