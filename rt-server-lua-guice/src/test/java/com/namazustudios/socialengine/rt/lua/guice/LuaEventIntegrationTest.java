package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.xodus.XodusContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class LuaEventIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLinkingAdvancedTest.class);

    private final TestJavaEvent tje = mock(TestJavaEvent.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
            .withNodeModule(new LuaModule())
            .withNodeModule(new XodusContextModule()
                    .withSchedulerThreads(1)
                    .withHandlerTimeout(3, MINUTES))
            .withNodeModule(new XodusEnvironmentModule()
                    .withTempEnvironments())
            .withNodeModule(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(TestJavaEvent.class).toInstance(tje);
                }
            })
            .withDefaultHttpClient()
            .start();

    private final Node node = getEmbeddedTestService().getNode();

    private final Context context = getEmbeddedTestService().getContext();

    @AfterClass
    public void teardown() {
        getEmbeddedTestService().close();
    }

    @Test()
    public void performEventTest() throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(2);
        doAnswer((invocation) -> {
            logger.info("invocation {}", invocation);
            countDownLatch.countDown();
            return null;
        }).when(tje).helloWorldEvent();
        getContext().getEventContext().postAsync("hello.event", Attributes.emptyAttributes());
        countDownLatch.await();
        verify(tje, times(2)).helloWorldEvent();
    }

    @Test()
    public void performEventTestWithArgs() throws Exception{
        final String who = "Krusty Krab";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            logger.info("invocation {}", invocation);
            countDownLatch.countDown();
            return null;
        }).when(tje).who(who);
        getContext().getEventContext().postAsync("who", Attributes.emptyAttributes(), who);
      countDownLatch.await();
        verify(tje, times(1)).who(who);
    }

    @Test()
    public void performEventTestWithMultipleArgs() throws Exception{
        final List<String> who = new ArrayList<>();
        who.add("Krusty");
        who.add("Krab");
        CountDownLatch countDownLatch = new CountDownLatch(2);
        doAnswer((invocation) -> {
            logger.info("invocation {}", invocation);
            countDownLatch.countDown();
            return null;
        }).when(tje).whoWithCount(anyString(), anyString());
        getContext().getEventContext().postAsync("who.with.count", Attributes.emptyAttributes(), who, who.size());
        countDownLatch.await();
        verify(tje, times(2)).whoWithCount(anyString(), anyString());
    }

    public JeroMQEmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public Node getNode() {
        return node;
    }

    public Context getContext() {
        return context;
    }
}
