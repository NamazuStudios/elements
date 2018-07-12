package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService;

import javax.ws.rs.client.Client;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class UnitTestModule extends AbstractModule {

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService();

    private final Client mockClient = mock(Client.class);

    private final Application spyApplication; {
        spyApplication = spy(Application.class);
        spyApplication.setId(randomUUID().toString());
        spyApplication.setName("Test");
        spyApplication.setDescription("Test Application.");
    }

    @Override
    protected void configure() {

        bind(Client.class).toInstance(mockClient);
        bind(Application.class).toInstance(spyApplication);
        bind(Context.class).toProvider(embeddedTestService::getContext);

        bind(JeroMQEmbeddedTestService.class).toInstance(embeddedTestService
            .withNodeModule(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Client.class).toInstance(mockClient);
                    bind(Application.class).toInstance(spyApplication);
                }
            }).start());

    }

}
