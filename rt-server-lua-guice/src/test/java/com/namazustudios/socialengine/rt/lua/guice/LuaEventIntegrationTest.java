package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.guice.ClasspathAssetLoaderModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

public class LuaEventIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLinkingAdvancedTest.class);

    private final EmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
            .withClient()
            .withApplicationNode()
                .withNodeModules(new LuaModule())
                .withNodeModules(new JavaEventModule())
                .withNodeModules(new ClasspathAssetLoaderModule().withDefaultPackageRoot())
            .endApplication()
            .withWorkerModule(new XodusEnvironmentModule().withTempSchedulerEnvironment())
            .withDefaultHttpClient()
        .start();

    private final ApplicationId testApplicationId = getEmbeddedTestService()
        .getWorker()
        .getApplicationId();

    private final Context context = getEmbeddedTestService()
        .getClient()
        .getContextFactory()
        .getContextForApplication(testApplicationId);

    private final TestJavaEvent tje = getEmbeddedTestService()
        .getWorker()
        .getIocResolver()
        .inject(TestJavaEvent.class);

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

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public Context getContext() {
        return context;
    }

}
