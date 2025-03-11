package dev.getelements.elements.rt.transact.unix;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.guice.SimpleExecutorsModule;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceModule;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import dev.getelements.elements.rt.transact.TransactionalSchedulerContextModule;
import org.mockito.stubbing.Answer;
import org.testng.annotations.*;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import static dev.getelements.elements.rt.Context.LOCAL;
import static dev.getelements.elements.sdk.cluster.id.NodeId.randomNodeId;
import static dev.getelements.elements.sdk.cluster.id.ResourceId.randomResourceIdForNode;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertTrue;

@Guice(modules = UnixFSTransactionalSchedulerContextTest.Module.class)
public class UnixFSTransactionalSchedulerContextTest {

    private NodeId nodeId;

    private Scheduler scheduler;

    private TaskService taskService;

    private ResourceService resourceService;

    private SchedulerContext schedulerContext;

    private PersistenceEnvironment environment;

    private List<Resource> resources;

    @BeforeClass
    public void start() {
        getTaskService().start();
        getEnvironment().start();
        getResourceService().start();
        getSchedulerContext().start();
    }

    @BeforeClass(dependsOnMethods = "start")
    public void createTestResources() {

        final var resources = new ArrayList<Resource>();

        final var path = new Path("/test/*");

        for (int i = 0; i < 100;++i) {
            final var resource = newMockResource();
            resources.add(resource);
            getResourceService().addAndAcquireResource(path.appendUUIDIfWildcard(), resource);
        }

        this.resources = unmodifiableList(resources);

    }

    @AfterClass
    public void stop() {
        getSchedulerContext().stop();
        getResourceService().stop();
        getEnvironment().stop();
        getTaskService().stop();
    }

    @DataProvider
    public Object[][] allResources() {
        return resources
                .stream()
                .map(r -> new Object[]{r})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "allResources")
    public void testResume(final Resource resource) throws Exception {

        final var taskId = new TaskId(resource.getId());
        final var exchanger = new Exchanger<Boolean>();

        final Answer<Void> answer = a -> {
            final TaskId actual = a.getArgument(0);
            final Double elapsed = a.getArgument(1);
            final boolean success = actual.equals(taskId) && elapsed > 0;
            exchanger.exchange(success);
            return null;
        };

        doAnswer(answer)
                .when(resource)
                .resumeFromScheduler(any(TaskId.class), anyDouble());

        getSchedulerContext().resumeTaskAfterDelay(taskId, 5, MILLISECONDS);

        final var success = exchanger.exchange(true);
        assertTrue(success);

    }

    protected Resource newMockResource() {
        final var resourceId = randomResourceIdForNode(nodeId);
        return newMockResource(resourceId);
    }

    public static Resource newMockResource(final ResourceId resourceId) {

        final var resource = mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);

        try {
            doAnswer(a -> {
                final var wbc = (WritableByteChannel) a.getArgument(0);
                final var bytes = resourceId.asBytes();
                final var buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) wbc.write(buffer);
                return null;
            }).when(resource).serialize(any(WritableByteChannel.class));
        } catch (IOException e) {
            // Should never happen in test code unless something is really wrong
            throw new UncheckedIOException(e);
        }

        return resource;

    }

    public static ResourceLoader mockResourceLoader() {

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);

        final Answer<Resource> loadAnswer = a -> {

            final ReadableByteChannel rbc = a.getArgument(0);
            final ByteBuffer byteBuffer = ByteBuffer.allocate(ResourceId.getSizeInBytes());
            while (byteBuffer.hasRemaining() && rbc.read(byteBuffer) >= 0);
            byteBuffer.rewind();

            final ResourceId resourceId = ResourceId.resourceIdFromByteBuffer(byteBuffer);
            return newMockResource(resourceId);

        };

        doAnswer(loadAnswer).when(resourceLoader).load(any(ReadableByteChannel.class));
        doAnswer(loadAnswer).when(resourceLoader).load(any(ReadableByteChannel.class), anyBoolean());

        return resourceLoader;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final NodeId testNodeId = randomNodeId();

            bind(NodeId.class).toInstance(testNodeId);

            install(new TransactionalSchedulerContextModule());
            install(new TransactionalResourceServiceModule().exposeTransactionalResourceService());
            install(new JournalTransactionalResourceServicePersistenceModule());

            install(new UnixFSTransactionalPersistenceContextModule()
                    .exposeDetailsForTesting()
                    .withTestingDefaults());


            bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
            bind(TaskService.class).to(SimpleTaskService.class).asEagerSingleton();

            install(new SimpleExecutorsModule().withDefaultSchedulerThreads());

            bind(ResourceLoader.class).toInstance(mockResourceLoader());

        }


    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    @Inject
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    @Inject
    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    @Inject
    public void setSchedulerContext(@Named(LOCAL) SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

    public PersistenceEnvironment getEnvironment() {
        return environment;
    }

    @Inject
    public void setEnvironment(PersistenceEnvironment environment) {
        this.environment = environment;
    }

}
